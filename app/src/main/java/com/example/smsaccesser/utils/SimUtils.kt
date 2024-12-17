package com.example.smsaccesser.utils

import android.content.Context
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat
import android.Manifest

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
}