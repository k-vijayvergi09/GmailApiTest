package com.example.gmailapitest.ui.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConsentScreen(
    onConsentGranted: () -> Unit,
    onConsentDenied: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bank Statement Access Requirements",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "The application requires access to your Gmail account to:" +
                    "\n• Identify bank statement emails" +
                    "\n• Extract financial transaction data" +
                    "\n• Provide spending insights",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onConsentGranted) {
            Text("Grant Access")
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onConsentDenied) {
            Text("Deny Access")
        }
    }
}
