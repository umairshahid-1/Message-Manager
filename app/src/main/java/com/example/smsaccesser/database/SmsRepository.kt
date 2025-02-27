package com.example.smsaccesser.database

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SmsRepository(private val smsDao: SmsDao) {

    // Fetch all messages from the database using Flow
    val allMessages: Flow<List<SmsEntity>> = smsDao.getAllMessages()

    // Save a new message to the database
    suspend fun saveMessage(message: SmsEntity) = withContext(Dispatchers.IO) {
        val existingMessage = smsDao.getMessageById(message.id)
        if (existingMessage == null) {
            Log.d("SmsRepository", "Inserting new message: ${message.id}")
            smsDao.insertMessages(listOf(message))
        } else {
            Log.d("SmsRepository", "Message already exists in database: ${message.id}")
        }
    }

    // Save multiple messages to the database (optimized bulk insert)
    suspend fun saveMessages(messages: List<SmsEntity>) = withContext(Dispatchers.IO) {
        Log.d("SmsRepository", "Bulk inserting ${messages.size} messages")
        smsDao.insertMessages(messages)
    }

    // Delete messages that are no longer present in the device's native SMS database
    suspend fun deleteMissingMessages(deviceMessageIds: List<Long>) = withContext(Dispatchers.IO) {
        smsDao.deleteMissingMessages(deviceMessageIds)
    }

    companion object {
        @Volatile
        private var INSTANCE: SmsRepository? = null

        fun getInstance(context: Context): SmsRepository {
            return INSTANCE ?: synchronized(this) {
                val database = SmsDatabase.getDatabase(context)
                SmsRepository(database.smsDao()).also { INSTANCE = it }
            }
        }
    }
}