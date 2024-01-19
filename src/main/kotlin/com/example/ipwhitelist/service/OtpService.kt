package com.example.ipwhitelist.service

import com.example.ipwhitelist.model.Otp
import com.example.ipwhitelist.repository.OtpRepository
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OtpService(
    private val otpRepository: OtpRepository,
) {
    private final val OTP_EXPIRATION = 60 * 5L

    fun generateOtp(userAgent: String, email: String): Otp {
        val otp = (10000..999999).random()
        val otpEntity = Otp(UUID.randomUUID(), email, userAgent, otp.toString(), OTP_EXPIRATION)
        otpRepository.save(otpEntity)
        return otpEntity
    }

    fun validateOtp(userAgent: String, email: String, otp: String) {
        val otpEntity = otpRepository.findByEmailAndUserAgent(email, userAgent) ?: throw NoSuchElementException("OTP not found")
        if (otpEntity.otp != otp) {
            throw InternalAuthenticationServiceException("Wrong OTP")
        }
    }
}