package com.example.smsaccesser.utils

import android.content.Context
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.telephony.SmsManager
import android.widget.Toast

object SimUtils {
    fun getSimSlot(
        context: Context,
        subscriptionManager: SubscriptionManager?,
        subscriptionId: Int
    ): String {
        // Explicitly check for READ_PHONE_STATE permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return "Permission Denied"
        }

        subscriptionManager?.activeSubscriptionInfoList?.forEach { info ->
            if (info.subscriptionId == subscriptionId) {
                return when (info.simSlotIndex) {
                    0 -> "SIM 1"
                    1 -> "SIM 2"
                    else -> "Unknown SIM"
                }
            }
        }
        return "Unknown SIM"
    }

    fun sendSms(context: Context, receiver: String, body: String, simSlot: String) {
        try {
            // Convert simSlot string to int, defaulting to 0 (SIM1) if invalid
            val subscriptionId = when (simSlot.trim()) {
                "2" -> 1  // SIM2
                else -> 0 // SIM1 (default)
            }

            val smsManager =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                        ?.createForSubscriptionId(subscriptionId)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                }

            smsManager?.sendTextMessage(receiver, null, body, null, null)
            Toast.makeText(context, "SMS sent successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send SMS: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}