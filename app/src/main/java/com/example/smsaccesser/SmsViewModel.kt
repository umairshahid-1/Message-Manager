package com.example.smsaccesser

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smsaccesser.database.SmsEntity
import com.example.smsaccesser.database.SmsRepository
import com.example.smsaccesser.utils.SimUtils.getContactName
import kotlinx.coroutines.launch
import com.example.smsaccesser.utils.SimUtils.getSimSlot

class SmsViewModel(context: Context) : ViewModel() {
    private val repository = SmsRepository.getInstance(context)

    // Observe messages in Room with LiveData for UI updates
    val messages = repository.allMessages

    // Track loading state
    val isLoading = mutableStateOf(true)

    // Load messages from the device and save them in Room
    fun loadDeviceMessages(context: Context) {
        viewModelScope.launch {
            isLoading.value = true
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val cursor = context.contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    arrayOf(
                        Telephony.Sms._ID,
                        Telephony.Sms.THREAD_ID,
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.BODY,
                        Telephony.Sms.DATE,
                        Telephony.Sms.TYPE,
                        Telephony.Sms.SUBSCRIPTION_ID // Fetch the SIM subscription ID
                    ),
                    null,
                    null,
                    "${Telephony.Sms.DATE} DESC"
                )

                cursor?.use {
                    val smsList = mutableListOf<SmsEntity>()
                    val subscriptionManager =
                        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                    val idColumn = it.getColumnIndex(Telephony.Sms._ID)
                    val threadIdColumn = it.getColumnIndex(Telephony.Sms.THREAD_ID)
                    val addressColumn = it.getColumnIndex(Telephony.Sms.ADDRESS)
                    val bodyColumn = it.getColumnIndex(Telephony.Sms.BODY)
                    val dateColumn = it.getColumnIndex(Telephony.Sms.DATE)
                    val typeColumn = it.getColumnIndex(Telephony.Sms.TYPE)
                    val subIdColumn = it.getColumnIndex(Telephony.Sms.SUBSCRIPTION_ID)

                    while (it.moveToNext()) {
                        val address = it.getString(addressColumn)
                        val contactName =
                            getContactName(context, address ?: "") // Resolve contact name
                        // Fetch SIM Slot Info
                        val subscriptionId = it.getInt(subIdColumn)
                        val simSlot = getSimSlot(context, subscriptionManager, subscriptionId)

                        val message = SmsEntity(
                            id = it.getLong(idColumn),
                            threadId = it.getLong(threadIdColumn),
                            address = address,
                            contactName = contactName, // Add resolved contact name
                            body = it.getString(bodyColumn),
                            date = it.getLong(dateColumn),
                            type = it.getInt(typeColumn),
                            simSlot = simSlot // Add SIM slot information
                        )
                        smsList.add(message)
                    }
                    repository.saveMessages(smsList) // Use saveMessages for bulk insertion
                }
            } else {
                Log.e("SmsViewModel", "READ_SMS or READ_CONTACTS permission not granted")
            }
            isLoading.value = false
        }
    }
}