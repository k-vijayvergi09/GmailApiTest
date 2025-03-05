package com.example.gmailapitest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gmailapitest.auth.AuthManager
import com.example.gmailapitest.ui.signin.MainScreen
import com.example.gmailapitest.ui.theme.GmailApiTestTheme
import androidx.credentials.CredentialManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val credentialManager by lazy {
        CredentialManager.create(this)
    }

    private val authManager by lazy {
        AuthManager(this, credentialManager, mainViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GmailApiTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val signInLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        try {
                            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                            val account = task.getResult(ApiException::class.java)
                            Log.d("MainActivity", "Sign in successful: ${account?.email}")

                            // Handle successful sign-in by initializing Gmail service
                            account?.let { googleAccount ->
                                handleSignInResult(googleAccount)
                            }
                        } catch (e: ApiException) {
                            Log.e("MainActivity", "Sign in failed", e)
                        }
                    }

                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        authManager = authManager,
                        signInLauncher = signInLauncher
                    )
                }
            }
        }
    }

    private fun handleSignInResult(account: GoogleSignInAccount) {
        // Start loading state
//        mainViewModel.setLoading(true)
        
        // Get the credential from AuthManager
        val credential = authManager.handleSignInResult(account)
        
        // Initialize Gmail service and handle results
        authManager.initializeGmailService(credential) { messageDetails ->
            // Update ViewModel with the messages
            mainViewModel.updateMessages(messageDetails)
            mainViewModel.setAuthenticated(true)
//            mainViewModel.setLoading(false)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GmailApiTestTheme {
        Greeting("Android")
    }
}