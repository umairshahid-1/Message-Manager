package com.example.smsaccesser.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun SendSmsScreen(
    onSendSms: (String, String, String) -> Unit
) {
    var receiverNumber by remember { mutableStateOf(TextFieldValue("")) }
    var messageBody by remember { mutableStateOf(TextFieldValue("")) }
    var simSlot by remember { mutableStateOf(TextFieldValue("1")) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = receiverNumber,
            onValueChange = { receiverNumber = it },
            label = { Text("Receiver Address") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        TextField(
            value = messageBody,
            onValueChange = { messageBody = it },
            label = { Text("Message Body") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        TextField(
            value = simSlot,
            onValueChange = { newValue ->
                if (newValue.text.isEmpty() || newValue.text in listOf("1", "2")) {
                    simSlot = newValue
                }
            },
            label = { Text("SIM Slot (1 or 2)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
        Button(
            onClick = { onSendSms(receiverNumber.text, messageBody.text, simSlot.text) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send SMS")
        }
    }
}