package com.example.ipwhitelist.service

import com.example.ipwhitelist.model.CreateUserRequest
import com.example.ipwhitelist.model.dynamodb.User
import com.example.ipwhitelist.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun createUser(createUserRequest: CreateUserRequest): User? {

        val userEntity = createUserRequest.toModel()
        println("Creating user: $userEntity")
        return userEntity
    }

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun findById(id: String): User? {
        return userRepository.findUserByUserId(id)
    }

    fun deleteById(id: String): Boolean {
        this.findById(id) ?: return false

        userRepository.deleteByUserId(id)
        return true
    }

    private fun CreateUserRequest.toModel() = User(
        userId = UUID.randomUUID().toString(),
        objectId = UUID.randomUUID().toString(),
        email = this.email,
        role = this.role,
        ip = "",
        otp = "",
        expiryDate = Instant.ofEpochMilli(System.currentTimeMillis() + 30000L).toString(),
        ttl = 30000L
    )

}