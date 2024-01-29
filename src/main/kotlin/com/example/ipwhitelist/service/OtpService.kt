package com.example.ipwhitelist.service

import com.example.ipwhitelist.model.CreateUserRequest
import com.example.ipwhitelist.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class OtpService(
    private val userRepository: UserRepository,
    private val userService: UserService
) {
    fun generateOtp(email: String): String {
        val otp = (10000..999999).random()

        var userEntity = userRepository.findByEmail(email = email)

        if (userEntity == null) {
            val createUserRequest = CreateUserRequest(email = email, role = "USER")
            userEntity = userService.createUser(createUserRequest)

            if (userEntity == null) {
                throw RuntimeException("Failed to generate OTP")
            }
        } else {
            throw RuntimeException("User already exists!")
        }

        userEntity.otp = otp.toString()
        userRepository.save(userEntity)

        return otp.toString()
    }


    fun validateOtp(email: String, otp: String): Boolean {
        val otpEntity = userRepository.findByEmail(email = email)
        println("OTP Matches: ${otpEntity?.otp == otp}")
        return otpEntity?.otp == otp
    }
}