package com.example.smsaccesser.utils

import android.content.Context
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.util.Log
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

    @SuppressLint("Range")
    fun getContactName(context: Context, phoneNumber: String): String? {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }
        }
        return null // Return null if no contact is found
    }

    fun sendSms(context: Context, receiver: String, body: String, simSlot: String) {
        try {
            // Convert SIM slot to subscription ID (0 for SIM1, 1 for SIM2)
            val subscriptionId = when (simSlot.trim()) {
                "2" -> 1 // SIM2
                else -> 0 // SIM1 (default)
            }

            // Get SmsManager for the subscription
            val smsManager =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                        ?.createForSubscriptionId(subscriptionId)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
                } ?: SmsManager.getDefault() // Fallback to default

            smsManager.sendTextMessage(receiver, null, body, null, null)
            Toast.makeText(context, "SMS sent!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("SimUtils", "Failed to send SMS: ${e.stackTraceToString()}")
            Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}