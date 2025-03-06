package com.example.gmailapitest.ui.signin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gmailapitest.MainViewModel
import com.example.gmailapitest.auth.AuthManager
import com.example.gmailapitest.auth.UserInfo
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    authManager: AuthManager,
    signInLauncher: ActivityResultLauncher<Intent>,
) {
    var userInfo by remember { mutableStateOf<UserInfo?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val mainViewModel: MainViewModel = viewModel()

    val messages by mainViewModel.messages.collectAsState()
    val isUserAuthenticated by mainViewModel.userAuthenticated.collectAsState()
    val context = LocalContext.current
    val isLoadingMessages by mainViewModel.loadingMessages.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isUserAuthenticated) {
            // Messages section
            if (messages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Credit Card Statements",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(messages) { message ->
                        MessageItem(message)
                    }
                }
            } else {
                Text("Loading messages...")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                sendBroadcastToNowBrief(context)
            }, enabled = !isLoadingMessages) { Text("Send to Now Brief") }
            Button(onClick = {
                scope.launch {
                    authManager.signOut()
                }
                mainViewModel.updateMessages(emptyList())
            }) { Text("Logout")}
        } else {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        try {
                            val signInIntent = authManager.signInClient.signInIntent
                            signInLauncher.launch(signInIntent)
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
}

@Composable
fun MessageItem(message: Map<String, String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
//            Text(
//                text = message["subject"] ?: "No Subject",
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold
//            )
//            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message["body"] ?: "No Body",
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "From: ${message["from"] ?: "Unknown Sender"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun sendBroadcastToNowBrief(context: Context) {
    val intent = Intent()
    intent.action = "com.example.gmailapitest.GMAIL_DATA"
    intent.putExtra("KeyName", "code1id") // Add extra data if needed

    // Optional: Set the component name for security
    intent.component = ComponentName(
        "com.samsung.android.app.smartsuggestions", // Package name of the receiving app
        "com.samsung.android.app.smartsuggestions.GmailDataReceiver" // Full class name of the receiver
    )

    try {
        context.sendBroadcast(intent)
        Log.d("sendBroadcastToOtherApp", "Broadcast sent successfully")
    } catch (e: Exception) {
        Log.e("sendBroadcastToOtherApp", "Error sending broadcast", e)
    }
}