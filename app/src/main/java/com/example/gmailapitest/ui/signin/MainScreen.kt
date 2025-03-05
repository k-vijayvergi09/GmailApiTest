package com.example.gmailapitest.ui.signin

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gmailapitest.auth.AuthManager
import com.example.gmailapitest.auth.UserInfo
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.gmail.GmailScopes
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    authManager: AuthManager,
    onSignInRequested: (Intent) -> Unit
) {
    var userInfo by remember { mutableStateOf<UserInfo?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            when {
                userInfo != null -> {
                    // Show user info and Gmail access button
                    Text("Welcome ${userInfo?.name}!")
                    Text("Email: ${userInfo?.email}")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    authManager.signOut()
                                    userInfo = null
                                } catch (e: Exception) {
                                    error = e.message ?: "Failed to sign out"
                                }
                            }
                        }
                    ) {
                        Text("Sign Out")
                    }
                }
                else -> {
                    // Show sign in button
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                error = null
                                try {
                                    val signInIntent = authManager.signInClient.signInIntent
                                    onSignInRequested(signInIntent)
                                } catch (e: Exception) {
                                    error = e.message ?: "Failed to start sign-in process"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        Text("Sign In with Google")
                    }
                }
            }

            error?.let { errorMessage ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 