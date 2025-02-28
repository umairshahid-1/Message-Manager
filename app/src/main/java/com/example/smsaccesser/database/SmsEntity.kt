package com.example.smsaccesser.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Locale

@Entity(tableName = "sms_table")
data class SmsEntity(
    @PrimaryKey val id: Long,
    val threadId: Long,       // Add Thread ID to group multipart messages
    val address: String?,
    val contactName: String?,
    val body: String,
    val type: Int,            // 1 for received, 2 for sent
    val date: Long,
    val simSlot: String       // SIM Slot information
) {
    // Helper function to format date as a string
    fun getDateString(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    // Helper function to get the day of the week
    fun getDayOfWeek(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault()) // "EEEE" returns full day name
        return sdf.format(date)
    }

    // Helper function to get time from the timestamp
    fun getTimeFromTimestamp(): String {
        val sdf =
            SimpleDateFormat("hh:mm a", Locale.getDefault()) // "hh:mm a" gives time like 02:35 PM
        return sdf.format(date)
    }
}