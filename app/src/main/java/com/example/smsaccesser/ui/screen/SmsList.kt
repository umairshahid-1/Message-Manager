package com.example.smsaccesser.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smsaccesser.database.SmsEntity
import com.example.smsaccesser.ui.DateHeader
import com.example.smsaccesser.ui.SmsItem
import com.example.smsaccesser.ui.ThreadHeader

@Composable
fun SmsList(
    messages: List<SmsEntity>,
    modifier: Modifier = Modifier,
    onNavigateToSendSms: () -> Unit
) {
    // Group messages by sender and threadId
    val groupedMessages = messages.groupBy { it.address to it.threadId }

    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = {
                Log.d("SendSmsButton", "Button clicked!")
                onNavigateToSendSms() // Ensure this function is implemented correctly
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Send SMS")
        }

        LazyColumn(modifier = modifier.padding(16.dp)) {
            groupedMessages.forEach { (senderThread, messagesInThread) ->
                val sender = messagesInThread.firstOrNull()?.contactName ?: senderThread.first ?: "Unknown Sender" // Sender's address

                // Thread Header: Display sender only
                item {
                    ThreadHeader(
                        sender = sender,
                        messageCount = messagesInThread.size
                    )
                }

                // Group messages within the thread by date
                val messagesByDate = messagesInThread.groupBy { it.getDateString() } // Dec 08, 2024
                messagesByDate.forEach { (date, messagesOnDate) ->
                    // Extract details from the first message for Date Header
                    val firstMessage = messagesOnDate.first()
                    val day = firstMessage.getDayOfWeek()          // E.g., "Fri"
                    val time = firstMessage.getTimeFromTimestamp() // E.g., "5:57 AM"
                    val simInfo = firstMessage.simSlot             // SIM slot info

                    // Date Header
                    item {
                        DateHeader(
                            date = date,
                            time = time,
                            day = day,
                            simInfo = simInfo
                        )
                    }

                    // Display messages under this date
                    items(messagesOnDate) { message ->
                        SmsItem(message)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}