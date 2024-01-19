package com.example.ipwhitelist.model

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)
