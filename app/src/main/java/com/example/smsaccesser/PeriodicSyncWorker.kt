package com.example.smsaccesser

import android.content.Context
import android.provider.Telephony
import android.telephony.SubscriptionManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smsaccesser.database.SmsEntity
import com.example.smsaccesser.database.SmsRepository
import com.example.smsaccesser.utils.SimUtils.getSimSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PeriodicSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val repository = SmsRepository.getInstance(applicationContext)
            val contentResolver = applicationContext.contentResolver

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
                Telephony.Sms.DEFAULT_SORT_ORDER
            )

            val messages = mutableListOf<SmsEntity>()
            val nativeMessageIds = mutableSetOf<Long>()

            cursor?.use {
                val subscriptionManager =
                    applicationContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val idColumn = it.getColumnIndex(Telephony.Sms._ID)
                val addressColumn = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyColumn = it.getColumnIndex(Telephony.Sms.BODY)
                val dateColumn = it.getColumnIndex(Telephony.Sms.DATE)
                val typeColumn = it.getColumnIndex(Telephony.Sms.TYPE)
                val threadIdColumn = it.getColumnIndex(Telephony.Sms.THREAD_ID)
                val subIdColumn = it.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)

                while (it.moveToNext()) {
                    // Fetch SIM Slot Info
                    val subscriptionId = it.getInt(subIdColumn)
                    val simSloT =
                        getSimSlot(applicationContext, subscriptionManager, subscriptionId)

                    val id = it.getLong(idColumn)
                    val address = it.getString(addressColumn)
                    val body = it.getString(bodyColumn)
                    val date = it.getLong(dateColumn)
                    val type = it.getInt(typeColumn)
                    val threadId = it.getLong(threadIdColumn)
                    val simSlot = simSloT // Add SIM slot information

                    nativeMessageIds.add(id)

                    val smsEntity = SmsEntity(
                        id = id,
                        threadId = threadId,
                        address = address,
                        body = body,
                        date = date,
                        type = type,
                        simSlot = simSlot
                    )
                    messages.add(smsEntity)
                }
            }

            // Sync messages with Room
            repository.saveMessages(messages)
            repository.deleteMissingMessages(nativeMessageIds.toList()) // Handle deleted messages

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}