package com.example.gmailapitest.auth

import android.accounts.Account
import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.gmailapitest.BuildConfig
import com.example.gmailapitest.MainViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class UserInfo(
    val id: String,
    val email: String,
    val name: String,
    val credential: GoogleIdTokenCredential
)

class AuthManager(
    private val context: Context,
    private val credentialManager: CredentialManager,
    private val mainViewModel: MainViewModel
) {
    private val verifier = GoogleIdTokenVerifier.Builder(
        NetHttpTransport(),
        GsonFactory()
    )
        .setAudience(listOf(BuildConfig.CLIENT_ID))
        .build()

    val signInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.CLIENT_ID)
            .requestEmail()
            .requestScopes(Scope(GmailScopes.GMAIL_READONLY))
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    private var currentUserInfo: UserInfo? = null

    fun handleSignInResult(account: GoogleSignInAccount): GoogleAccountCredential {
        Log.d("AuthManager", "Handling sign-in result for account: ${account.email}")
        
        // Create a GoogleAccountCredential for Gmail API access
        val googleAccountCredential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(GmailScopes.GMAIL_READONLY)
        ).apply {
            setSelectedAccount(Account(account.email, "com.google"))
        }

        return googleAccountCredential

        // Initialize Gmail service
//        initializeGmailService(googleAccountCredential)
    }

    // Contains OAuth flow from search result [4][8]
    suspend fun authenticate(): UserInfo? = withContext(Dispatchers.IO) {
        try {
            Log.d("AuthManager", "Starting authentication process")
            Log.d("AuthManager", "Client ID: ${BuildConfig.CLIENT_ID}")

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .build()

            Log.d("AuthManager", "Created GoogleIdOption")
            val request: GetCredentialRequest =
                GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
            Log.d("AuthManager", "Created GetCredentialRequest")

            try {
                val response: GetCredentialResponse = credentialManager.getCredential(
                    context = context,
                    request = request
                )
                Log.d("AuthManager", "Got credential response")

                // Log the credential type and details
                val credential = response.credential

                when (credential) {
                    is CustomCredential -> {
                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                val idToken = verifier.verify(googleCredential.idToken)
                                
                                if (idToken != null) {
                                    val payload = idToken.payload
                                    UserInfo(
                                        id = payload.subject,
                                        email = payload.email ?: "",
                                        name = "User",
                                        credential = googleCredential
                                    )
                                } else {
                                    Log.e("AuthManager", "Failed to verify ID token")
                                    null
                                }
                            } catch (e: GoogleIdTokenParsingException) {
                                Log.e(
                                    "CustomCredential",
                                    "Received an invalid google id token response",
                                    e
                                )
                                null
                            }
                        } else {
                            // Catch any unrecognized custom credential type here.
                            Log.e("CustomCredential", "Unexpected type of credential")
                            null
                        }
                    }
                    else -> {
                        Log.e(
                            "AuthManager",
                            "Unexpected credential type: ${credential?.javaClass?.simpleName}"
                        )
                        Log.e("AuthManager", "Credential details: $credential")
                        null
                    }
                }
            } catch (e: NoCredentialException) {
                Log.e(
                    "AuthManager",
                    "No credentials available. Please check Google Play Services and app configuration.",
                    e
                )
                throw e
            } catch (e: Exception) {
                Log.e("AuthManager", "Error getting credential", e)
                throw e
            }
        } catch (e: GetCredentialException) {
            Log.e("AuthManager", "GetCredentialException during authentication", e)
            throw e
        } catch (e: Exception) {
            Log.e("AuthManager", "Unexpected error during authentication", e)
            throw e
        }
    }

    suspend fun signOut() {
        try {
            Log.d("AuthManager", "Starting sign out process")
            signInClient.signOut()
            currentUserInfo = null
            mainViewModel.setAuthenticated(false)
            Log.d("AuthManager", "Successfully signed out")
        } catch (e: Exception) {
            Log.e("AuthManager", "Error during sign out", e)
            throw e
        }
    }

    // Helper function to extract the message body
    fun getMessageBody(message: Message): String {
        val payload = message.payload
        return when {
            payload.body.data != null -> {
                Base64.decode(payload.body.data, Base64.URL_SAFE).toString(Charsets.UTF_8)
            }
            payload.parts != null -> {
                payload.parts.find { part ->
                    part.mimeType == "text/plain" || part.mimeType == "text/html"
                }?.let { part ->
                    Base64.decode(part.body.data, Base64.URL_SAFE).toString(Charsets.UTF_8)
                } ?: "No readable content"
            }
            else -> "No content"
        }
    }

    fun initializeGmailService(credential: GoogleAccountCredential, onMessagesLoaded: (List<Map<String, String>>) -> Unit = {}) {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        val gmailService = Gmail.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("GmailApiTest")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = "me"
                // Search for messages with "Credit card statement" in the subject
                val query = "subject:\"Credit card statement\""
                val listResponse = gmailService.users().messages().list(user)
                    .setQ(query)
                    .setMaxResults(10)
                    .execute()
                
                val messages = listResponse.messages
                val messageDetails = mutableListOf<Map<String, String>>()

                messages?.forEach { message ->
                    val msg = gmailService.users().messages().get(user, message.id).execute()
                    val subject = msg.payload.headers.find { it.name == "Subject" }?.value ?: "No subject"
                    val from = msg.payload.headers.find { it.name == "From" }?.value ?: "Unknown sender"
                    val body = getMessageBody(msg)

                    Log.d("GmailApiService", "subject $subject, from $from")
                    messageDetails.add(mapOf("subject" to subject, "from" to from, "id" to message.id, "body" to body))
                }
                
                // Switch to main thread to call the callback function
                withContext(Dispatchers.Main) {
                    onMessagesLoaded(messageDetails)
                }
            } catch (e: Exception) {
                Log.e("GmailApiService", "Error accessing Gmail", e)
                withContext(Dispatchers.Main) {
                    onMessagesLoaded(emptyList())
                }
            }
        }
    }
}
