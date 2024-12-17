package com.example.smsaccesser.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smsaccesser.database.SmsEntity

@Composable
fun SmsList(messages: List<SmsEntity>, modifier: Modifier = Modifier) {
    // Group messages by sender and threadId
    val groupedMessages = messages.groupBy { it.address to it.threadId }

    LazyColumn(modifier = modifier.padding(16.dp)) {
        groupedMessages.forEach { (senderThread, messagesInThread) ->
            val sender = senderThread.first ?: "Unknown Sender" // Sender's address

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