package com.example.smsaccesser

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.smsaccesser.ui.PermissionDeniedMessage
import com.example.smsaccesser.ui.SmsList
import com.example.smsaccesser.ui.theme.SmsAccesserTheme

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

                Scaffold { innerPadding ->
                    when {
                        isLoading -> {
                            // Show a loading spinner while messages are being fetched
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
                            // Show messages once loaded
                            SmsList(
                                messages = messages, // Pass the raw list of messages
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        else -> {
                            // Show permission denied message
                            PermissionDeniedMessage()
                        }
                    }
                }
            }
        }

        if (checkSmsPermission()) {
            // Load messages if permission is already granted
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
                Manifest.permission.READ_PHONE_STATE
            )
        )
    }
}