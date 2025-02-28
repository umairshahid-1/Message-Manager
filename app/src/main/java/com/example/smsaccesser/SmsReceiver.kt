package com.example.smsaccesser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.core.content.ContextCompat

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.d("SmsReceiver", "New SMS received. Triggering sync...")
            // Notify SmsService to sync
            val serviceIntent = Intent(context, SmsService::class.java).apply {
                action = "FORCE_SYNC"
            }
            ContextCompat.startForegroundService(context!!, serviceIntent)
        }
    }
}