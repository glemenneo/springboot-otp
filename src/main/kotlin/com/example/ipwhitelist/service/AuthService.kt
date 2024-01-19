package com.example.ipwhitelist.service

import org.springframework.stereotype.Service

@Service
class AuthService(
    private val otpService: OtpService
) {
        fun requestOtp(email: String): Boolean {
            // SEND OTP BY SES
            return otpService.generateOtp(email)
        }

        fun verifyOtp(email: String, otp: String): Boolean {
            return otpService.validateOtp(email, otp)
        }
}