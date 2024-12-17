package com.example.smsaccesser.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessages(messages: List<SmsEntity>)

    @Query("SELECT * FROM sms_table ORDER BY date DESC")
    fun getAllMessages(): Flow<List<SmsEntity>>

    @Query("SELECT * FROM sms_table WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: Long): SmsEntity?

    @Query("DELETE FROM sms_table WHERE id NOT IN (:ids)")
    suspend fun deleteMissingMessages(ids: List<Long>)
}