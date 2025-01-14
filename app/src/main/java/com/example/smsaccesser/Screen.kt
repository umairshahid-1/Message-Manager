package com.example.smsaccesser

sealed class Screen {
    // Object represents a singleton instance for each screen
    object SmsList : Screen()
    object SendSms : Screen()
}