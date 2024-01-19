package com.example.ipwhitelist.service

import org.springframework.stereotype.Service

@Service
class AuthService(
    private val otpService: OtpService
) {
        fun requestOtp(email: String) {
            val otpEntity = otpService.generateOtp(email)
            val otp = otpEntity.otp
            // SEND OTP BY SES
            println("OTP: $otp")
        }

        fun verifyOtp(email: String, otp: String): Boolean {
            return otpService.validateOtp(email, otp)
        }
}