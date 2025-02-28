package com.example.smsaccesser

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.IBinder
import android.provider.Telephony
import android.util.Log
import com.example.smsaccesser.database.SmsEntity
import com.example.smsaccesser.database.SmsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsService : Service() {

    private lateinit var repository: SmsRepository

    override fun onCreate() {
        super.onCreate()
        repository = SmsRepository.getInstance(applicationContext)
        createNotificationChannel()
        startForeground(1, createNotification())

        // Register the ContentObserver to monitor changes in the SMS database.
        contentResolver.registerContentObserver(
            Telephony.Sms.CONTENT_URI,
            true, // Set to true to observe descendant URIs (e.g., Inbox, Sent)
            smsContentObserver
        )

        // Run an initial sync for both new and deleted messages at startup.
        CoroutineScope(Dispatchers.IO).launch {
            syncMessagesWithDevice()
            syncDeletedMessages()
        }
    }

    // Updated ContentObserver: on every change, sync both new and deleted messages.
    private val smsContentObserver = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            Log.d("SmsService", "SMS ContentObserver triggered: $uri")
            CoroutineScope(Dispatchers.IO).launch {
                syncMessagesWithDevice()
                syncDeletedMessages()  // This ensures deleted messages are removed in real time.
            }
        }
    }

    // Also trigger a full sync when the service receives a FORCE_SYNC action.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "FORCE_SYNC") {
            CoroutineScope(Dispatchers.IO).launch {
                syncMessagesWithDevice()  // Sync new or updated messages.
                syncDeletedMessages()       // Also sync deletions immediately.
            }
        }
        return START_STICKY
    }

    // Sync new and updated messages from the system SMS database into your ROOM database.
    private suspend fun syncMessagesWithDevice() {
        Log.d("SmsService", "Syncing messages from device...")
        val deviceMessageIds = mutableListOf<Long>()

        try {
            // Get the native SMS messages using a query on Telephony.Sms.CONTENT_URI.
            val cursor = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE,
                    Telephony.Sms.THREAD_ID,
                    Telephony.Sms.SUBSCRIPTION_ID
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            val deviceMessages = mutableListOf<SmsEntity>()
            cursor?.use {
                val idColumn = it.getColumnIndex(Telephony.Sms._ID)
                val threadIdColumn = it.getColumnIndex(Telephony.Sms.THREAD_ID)
                val addressColumn = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyColumn = it.getColumnIndex(Telephony.Sms.BODY)
                val dateColumn = it.getColumnIndex(Telephony.Sms.DATE)
                val typeColumn = it.getColumnIndex(Telephony.Sms.TYPE)
                val subIdColumn = it.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)

                while (it.moveToNext()) {
                    val address = it.getString(addressColumn)
                    // You may resolve the contact name as before.
                    val contactName = com.example.smsaccesser.utils.SimUtils.getContactName(
                        this@SmsService,
                        address ?: ""
                    )

                    val smsEntity = SmsEntity(
                        id = it.getLong(idColumn),
                        threadId = it.getLong(threadIdColumn),
                        address = address,
                        contactName = contactName,
                        body = it.getString(bodyColumn),
                        date = it.getLong(dateColumn),
                        type = it.getInt(typeColumn),
                        simSlot = com.example.smsaccesser.utils.SimUtils.getSimSlot(
                            this@SmsService,
                            getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as? android.telephony.SubscriptionManager,
                            it.getInt(subIdColumn)
                        )
                    )
                    deviceMessages.add(smsEntity)
                    deviceMessageIds.add(it.getLong(idColumn))
                }
            }

            // Save or update messages in your ROOM database.
            repository.saveMessages(deviceMessages)
        } catch (e: Exception) {
            Log.e("SmsService", "Sync failed: ${e.message}")
        }
    }

    // New: Sync deletion of messages from your ROOM database by comparing with the system database.
    @SuppressLint("Range")
    private suspend fun syncDeletedMessages() {
        Log.d("SmsService", "Syncing deletions...")
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms._ID),
            null,
            null,
            null
        )

        // Gather all native SMS message IDs.
        val nativeIds = mutableSetOf<Long>()
        cursor?.use {
            val idColumn = it.getColumnIndex(Telephony.Sms._ID)
            while (it.moveToNext()) {
                nativeIds.add(it.getLong(idColumn))
            }
        }

        // Remove messages from the ROOM database that no longer exist in the native database.
        repository.deleteMissingMessages(nativeIds.toList())
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

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(smsContentObserver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}