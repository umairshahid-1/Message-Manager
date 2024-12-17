package com.example.smsaccesser

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Telephony
import android.telephony.SubscriptionManager
import com.example.smsaccesser.database.SmsEntity
import com.example.smsaccesser.database.SmsRepository
import com.example.smsaccesser.utils.SimUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SmsService : Service() {

    private lateinit var repository: SmsRepository

    override fun onCreate() {
        super.onCreate()
        repository = SmsRepository.getInstance(applicationContext)
        createNotificationChannel()
        startForeground(1, createNotification())
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "SMS_CHANNEL_ID",
            "SMS Listener",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return Notification.Builder(this, "SMS_CHANNEL_ID")
            .setContentTitle("SMS Listener")
            .setContentText("Listening to SMS in real time...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        listenForNewMessages()
        return START_STICKY
    }

    private fun listenForNewMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    syncMessagesWithDevice()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(60000) // Wait for 1 minute before syncing again
            }
        }
    }

    private suspend fun syncMessagesWithDevice() {
        val deviceMessageIds = mutableListOf<Long>()

        // Get the SubscriptionManager instance
        val subscriptionManager =
            getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        // Query the native SMS database
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.THREAD_ID,       // Fetch THREAD_ID
                Telephony.Sms.SUBSCRIPTION_ID  // Fetch SIM subscription ID
            ),
            null,
            null,
            "${Telephony.Sms.DATE} ASC" // Sort by ascending date
        )

        // Use a map to group messages by THREAD_ID
        val messageMap = mutableMapOf<Long, SmsEntity>()

        cursor?.use {
            val idColumn = it.getColumnIndex(Telephony.Sms._ID)
            val threadIdColumn = it.getColumnIndex(Telephony.Sms.THREAD_ID)
            val addressColumn = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyColumn = it.getColumnIndex(Telephony.Sms.BODY)
            val dateColumn = it.getColumnIndex(Telephony.Sms.DATE)
            val typeColumn = it.getColumnIndex(Telephony.Sms.TYPE)
            val subIdColumn = it.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val threadId = it.getLong(threadIdColumn) // Group by THREAD_ID
                val address = it.getString(addressColumn)
                val body = it.getString(bodyColumn)
                val date = it.getLong(dateColumn)
                val type = it.getInt(typeColumn)
                val subscriptionId = it.getInt(subIdColumn)

                // Map subscription ID to SIM slot
                val simSlot =
                    SimUtils.getSimSlot(this@SmsService, subscriptionManager, subscriptionId)

                // Combine messages with the same THREAD_ID
                if (messageMap.containsKey(threadId)) {
                    val existingMessage = messageMap[threadId]
                    val combinedBody = existingMessage?.body + " " + body // Append the body
                    messageMap[threadId] = existingMessage?.copy(body = combinedBody) ?: continue
                } else {
                    // Create a new SmsEntity
                    val smsEntity = SmsEntity(
                        id = id,
                        threadId = threadId,
                        address = address,
                        body = body,
                        date = date,
                        type = type,
                        simSlot = simSlot
                    )
                    messageMap[threadId] = smsEntity
                }

                deviceMessageIds.add(id)
            }
        }

        // Sync with Room database
        repository.saveMessages(messageMap.values.toList()) // Insert combined messages
        repository.deleteMissingMessages(deviceMessageIds) // Remove messages not in the device
    }

    override fun onBind(intent: Intent?): IBinder? = null
}