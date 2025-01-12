package com.example.smsaccesser

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.example.smsaccesser.database.SmsEntity
import com.example.smsaccesser.database.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            smsMessages?.forEach { message ->
                val repository = SmsRepository.getInstance(context!!)
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d("SmsReceiver", "SMS Received: ${message.messageBody}")

                    val smsEntity = fetchSmsFromDatabase(context.contentResolver, message)
                        ?: SmsEntity(
                            id = System.currentTimeMillis(), // Fallback to timestamp
                            threadId = message.originatingAddress.hashCode()
                                .toLong(), // Fallback to address hash
                            address = message.originatingAddress,
                            body = message.messageBody,
                            date = message.timestampMillis,
                            type = 1,
                            simSlot = "SIM 1" // Placeholder
                        )

                    repository.saveMessages(listOf(smsEntity))
                    Log.d("SmsReceiver", "SMS saved to database: $smsEntity")
                }
            }
        }
    }

    @SuppressLint("Range")
    private suspend fun fetchSmsFromDatabase(
        contentResolver: ContentResolver,
        message: SmsMessage
    ): SmsEntity? {
        var retries = 0
        while (retries < 3) {
            Log.d("SmsReceiver", "Attempt $retries to fetch SMS from database")
            val cursor = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.THREAD_ID
                ),
                "${Telephony.Sms.ADDRESS} = ? AND ${Telephony.Sms.BODY} = ? AND ${Telephony.Sms.DATE} = ?",
                arrayOf(
                    message.originatingAddress,
                    message.messageBody,
                    message.timestampMillis.toString()
                ),
                null
            )

            cursor?.use {
                if (it.moveToFirst()) {
                    val id = it.getLong(it.getColumnIndex(Telephony.Sms._ID))
                    val threadId = it.getLong(it.getColumnIndex(Telephony.Sms.THREAD_ID))
                    return SmsEntity(
                        id = id,
                        threadId = threadId,
                        address = message.originatingAddress,
                        body = message.messageBody,
                        date = message.timestampMillis,
                        type = 1,
                        simSlot = "SIM 1" // Placeholder
                    )
                }
            }

            retries++
            delay(100) // Wait 100ms before retrying
        }
        Log.w("SmsReceiver", "Failed to fetch SMS from database after retries")
        return null
    }
}