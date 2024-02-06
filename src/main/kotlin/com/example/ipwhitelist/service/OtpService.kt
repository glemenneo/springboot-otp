package com.example.ipwhitelist.service

import com.example.ipwhitelist.model.CreateUserRequest
import com.example.ipwhitelist.model.dynamodb.UserOtp
import com.example.ipwhitelist.model.dynamodb.UserTableKeyPrefix
import com.example.ipwhitelist.repository.UserRepository
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant
import java.util.*

@Service
class OtpService(
    private val userRepository: UserRepository, private val userService: UserService
) {
    companion object {
        private const val OTP_MAX = 999999
    }

    fun generateOtp(email: String): String {
        val otp = SecureRandom().nextInt(OTP_MAX).toString().padStart(6, '0')
        // Check if the user already exists
        var user = userService.findByEmail(email)

        if (user == null) {
            val createUserRequest = CreateUserRequest(email = email, role = "USER")
            user = userService.createUser(createUserRequest)

            if (user == null) {
                throw RuntimeException("Failed to generate OTP")
            }
        }

        val userOtp = UserOtp(
            userId = "${UserTableKeyPrefix.USER.prefix}${user.id}",
            objectId = "${UserTableKeyPrefix.OTP.prefix}${UUID.randomUUID()}",
            otp = otp,
            expiryDate = Instant.ofEpochMilli(System.currentTimeMillis() + 300000).toString(),
            ttl = 300000
        )
        userRepository.save(userOtp)

        return otp
    }

    fun validateOtp(email: String, otp: String): Boolean {
        val otpEntity = userRepository.findUserOtpByEmail(email = email)
        return otpEntity?.otp == otp
    }
}