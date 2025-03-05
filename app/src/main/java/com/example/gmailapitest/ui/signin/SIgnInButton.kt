package com.example.gmailapitest.ui.signin

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gmailapitest.auth.AuthManager
import com.example.gmailapitest.auth.UserInfo
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.exceptions.GetCredentialCustomException

@Composable
fun GoogleSignInButton(
    authManager: AuthManager,
    onSignInResult: (UserInfo?) -> Unit
) {
    val context = LocalContext.current
    val scope = remember { CoroutineScope(Dispatchers.Main) }

    Button(
        onClick = {
            scope.launch {
                try {
                    Log.d("SignInButton", "Starting sign in process")
                    val userInfo = authManager.authenticate()
                    if (userInfo == null) {
                        Log.e("SignInButton", "No valid credential returned")
                        Toast.makeText(
                            context,
                            "Sign-in failed: Unable to get valid credentials. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }
                    Log.d("SignInButton", "Successfully authenticated")
                    onSignInResult(userInfo)
                } catch (e: GetCredentialCustomException) {
                    Log.e("SignInButton", "GetCredentialCustomException during sign in", e)
                    val errorMessage = when {
                        e.message?.contains("[28444]") == true -> {
                            "Sign-in failed: OAuth consent screen issue. Please check:\n" +
                            "1. Your account is added as a test user\n" +
                            "2. Gmail API is enabled in Google Cloud Console\n" +
                            "3. Try signing in with a different Google account"
                        }
                        else -> "Sign-in failed: ${e.message}"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                } catch (e: NoCredentialException) {
                    Log.e("SignInButton", "NoCredentialException during sign in", e)
                    Toast.makeText(
                        context,
                        "Sign-in failed: No credentials available. Please check Google Play Services.",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Log.e("SignInButton", "Unexpected error during sign in", e)
                    Toast.makeText(
                        context,
                        "Sign-in failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Sign in with Google")
    }
}
