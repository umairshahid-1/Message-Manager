package com.example.smsaccesser.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

@Composable
fun ThreadHeader(sender: String, messageCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Messages from: $sender",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "$messageCount messages",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun DateHeader(date: String, day: String, time: String, simInfo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$date $day $time  $simInfo",
            style = TextStyle(
                fontSize = 12.sp,               // Smaller font size
                color = Color.Gray,             // Light gray text color
                fontWeight = FontWeight.Normal  // Optional: Normal weight for a subtle look
            )
        )
    }
}