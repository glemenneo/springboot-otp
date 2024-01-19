package com.example.ipwhitelist.service

import com.example.ipwhitelist.model.Otp
import com.example.ipwhitelist.repository.OtpRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class OtpService(
    private val otpRepository: OtpRepository,
) {
    private final val OTP_EXPIRATION = 60 * 5L

    fun generateOtp(email: String): Otp {
        val otp = (10000..999999).random()
        val otpEntity = Otp(UUID.randomUUID(), email, otp.toString(), OTP_EXPIRATION)
        return otpEntity
    }

    fun validateOtp(email: String, otp: String): Boolean {
        val otpEntity = otpRepository.findByEmail(email)
        return otpEntity?.otp == otp
    }
}