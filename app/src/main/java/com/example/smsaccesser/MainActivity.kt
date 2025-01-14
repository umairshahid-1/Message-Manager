package com.example.smsaccesser

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.smsaccesser.ui.PermissionDeniedMessage
import com.example.smsaccesser.ui.screen.SendSmsScreen
import com.example.smsaccesser.ui.screen.SmsList
import com.example.smsaccesser.ui.theme.SmsAccesserTheme
import com.example.smsaccesser.utils.SimUtils

class MainActivity : ComponentActivity() {
    private lateinit var smsViewModel: SmsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create ViewModel using the factory
        val factory = SmsViewModelFactory(applicationContext)
        smsViewModel = ViewModelProvider(this, factory)[SmsViewModel::class.java]

        setContent {
            SmsAccesserTheme {
                val messages by smsViewModel.messages.collectAsState(initial = emptyList())
                val isLoading by smsViewModel.isLoading
                var currentScreen by remember { mutableStateOf<Screen>(Screen.SmsList) }

                LaunchedEffect(currentScreen) {
                    Log.d("Navigation", "Current screen: $currentScreen")
                }

                Scaffold { innerPadding ->
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        checkSmsPermission() -> {
                            Box(modifier = Modifier.padding(innerPadding)) {
                                when (currentScreen) {
                                    Screen.SmsList -> SmsList(
                                        messages = messages,
                                        modifier = Modifier,
                                        onNavigateToSendSms = {
                                            Log.d(
                                                "Navigation",
                                                "Attempting to navigate to SendSms screen"
                                            )
                                            currentScreen = Screen.SendSms
                                        }
                                    )

                                    Screen.SendSms -> SendSmsScreen(
                                        onSendSms = { receiver, body, simSlot ->
                                            Log.d("Navigation", "Sending SMS and returning to list")
                                            SimUtils.sendSms(
                                                this@MainActivity,
                                                receiver,
                                                body,
                                                simSlot
                                            )
                                            // Refresh messages after sending
                                            smsViewModel.loadDeviceMessages(this@MainActivity)
                                            currentScreen = Screen.SmsList
                                        }
                                    )
                                }
                            }
                        }

                        else -> {
                            PermissionDeniedMessage()
                        }
                    }
                }
            }
        }

        if (checkSmsPermission()) {
            smsViewModel.loadDeviceMessages(this)
        } else {
            requestSmsPermissions()
        }
    }

    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.READ_SMS] == true
        val phoneStateGranted = permissions[Manifest.permission.READ_PHONE_STATE] == true

        if (smsGranted && phoneStateGranted) {
            smsViewModel.loadDeviceMessages(this)
        } else {
            requestSmsPermissions()
        }
    }

    private fun requestSmsPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS
            )
        )
    }
}