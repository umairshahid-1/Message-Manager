package com.example.smsaccesser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the broadcast received is for SMS
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent.action) {
            val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in smsMessages) {
                val sender = message.displayOriginatingAddress
                val body = message.messageBody
                val messageType = 1 // Typically 1 for received messages

                // Start SmsService to save the message in the database
                val serviceIntent = Intent(context, SmsService::class.java).apply {
                    putExtra("sms_sender", sender)
                    putExtra("sms_body", body)
                    putExtra("sms_type", messageType)
                }
                context.startService(serviceIntent)
            }
        }
    }
}