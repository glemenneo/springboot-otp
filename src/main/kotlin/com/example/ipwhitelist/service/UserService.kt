package com.example.ipwhitelist.service

import com.example.ipwhitelist.model.CreateUserRequest
import com.example.ipwhitelist.model.dynamodb.DataClassMappings
import com.example.ipwhitelist.model.dynamodb.User
import com.example.ipwhitelist.model.dynamodb.UserPrincipal
import com.example.ipwhitelist.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun createUser(createUserRequest: CreateUserRequest): UserPrincipal? {
        val userEntity = createUserRequest.toModel()
        println("Creating user: $userEntity")
        userRepository.save(userEntity)
        return userEntity
    }

    fun findByEmail(email: String): UserPrincipal? {
        return userRepository.findUserPrincipalByEmail(email)
    }

    fun findById(id: String): User? {
        return userRepository.findUserByUserId(id)
    }

    fun deleteById(id: String): Boolean {
        this.findById(id) ?: return false

        userRepository.deleteByUserId(id)
        return true
    }

    private fun CreateUserRequest.toModel() = UserPrincipal(
        userId = UUID.randomUUID().toString(),
        objectId = DataClassMappings.USER_PRINCIPAL_PREFIX+ UUID.randomUUID().toString(),
        email = this.email,
        role = "USER"
    )

}