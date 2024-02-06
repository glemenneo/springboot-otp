package com.example.ipwhitelist.service

import com.example.ipwhitelist.model.VerifyOtpRequest
import org.springframework.stereotype.Service
import java.util.Date

@Service
class AuthService(
    private val otpService: OtpService,
    private val jwtService: JwtService
) {
    fun requestOtp(email: String) {
        val otp = otpService.generateOtp(email)
        // SEND OTP BY SES
        println("OTP: $otp")
    }

    fun verifyOtp(verifyOtpRequest: VerifyOtpRequest): String? {
        val isOtpValid = otpService.validateOtp(verifyOtpRequest.email, verifyOtpRequest.otp)
        if (!isOtpValid) {
            return null
        }

        val expirationDate = Date(System.currentTimeMillis() + 30 * 60 * 1000)
        return jwtService.generateToken(verifyOtpRequest.email, expirationDate)
    }
}