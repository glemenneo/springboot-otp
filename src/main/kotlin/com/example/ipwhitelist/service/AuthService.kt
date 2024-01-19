package com.example.ipwhitelist.service

import org.springframework.stereotype.Service

@Service
class AuthService(
    private val otpService: OtpService
) {
    fun requestOtp(userAgent: String, email: String) {
        val otpEntity = otpService.generateOtp(userAgent, email)
        val otp = otpEntity.otp
        // SEND OTP BY SES
        println("OTP: $otp")
    }

    fun verifyOtp(userAgent: String, email: String, otp: String) {
        otpService.validateOtp(userAgent, email, otp)
    }
}