package com.example.smsaccesser

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Telephony
import androidx.work.*
import com.example.smsaccesser.database.SmsEntity
import com.example.smsaccesser.database.SmsRepository
import com.example.smsaccesser.utils.SimUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SmsService : Service() {

    private lateinit var repository: SmsRepository

    override fun onCreate() {
        super.onCreate()
        repository = SmsRepository.getInstance(applicationContext)
        createNotificationChannel()
        startForeground(1, createNotification())
        schedulePeriodicSync()
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
        CoroutineScope(Dispatchers.IO).launch {
            syncDeletedMessages()
        }
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
                kotlinx.coroutines.delay(60000) // Wait for 1 minute before syncing again
            }
        }
    }

    private suspend fun syncMessagesWithDevice() {
        val deviceMessageIds = mutableListOf<Long>()

        // Get SubscriptionManager instance
        val subscriptionManager =
            getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as android.telephony.SubscriptionManager

        // Query the native SMS database
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
                val id = it.getLong(idColumn)
                val threadId = it.getLong(threadIdColumn)
                val address = it.getString(addressColumn)
                val body = it.getString(bodyColumn)
                val date = it.getLong(dateColumn)
                val type = it.getInt(typeColumn)
                val subscriptionId = it.getInt(subIdColumn)

                val simSlot =
                    SimUtils.getSimSlot(this@SmsService, subscriptionManager, subscriptionId)

                val smsEntity = SmsEntity(
                    id = id,
                    threadId = threadId,
                    address = address,
                    body = body,
                    date = date,
                    type = type,
                    simSlot = simSlot
                )
                deviceMessages.add(smsEntity)
                deviceMessageIds.add(id)
            }
        }

        // Sync messages to Room database
        repository.saveMessages(deviceMessages)
        repository.deleteMissingMessages(deviceMessageIds)
    }

    @SuppressLint("Range")
    private suspend fun syncDeletedMessages() {
        val cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms._ID),
            null,
            null,
            null
        )

        val nativeIds = mutableSetOf<Long>()
        cursor?.use {
            while (it.moveToNext()) {
                nativeIds.add(it.getLong(it.getColumnIndex(Telephony.Sms._ID)))
            }
        }

        repository.deleteMissingMessages(nativeIds.toList())
    }

    private fun schedulePeriodicSync() {
        val workRequest = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "SmsSyncWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null
}