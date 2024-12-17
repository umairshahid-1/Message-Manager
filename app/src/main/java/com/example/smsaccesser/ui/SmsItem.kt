package com.example.smsaccesser.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smsaccesser.database.SmsEntity

@Composable
fun SmsItem(message: SmsEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = if (message.type == 1) "Received" else "Sent",
            style = MaterialTheme.typography.labelMedium,
            color = if (message.type == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "From: ${message.address ?: "Unknown"}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message.body,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}