package com.example.ipwhitelist.service

import com.example.ipwhitelist.model.CreateUserRequest
import com.example.ipwhitelist.model.UserResponse
import com.example.ipwhitelist.model.dynamodb.UserPrincipal
import com.example.ipwhitelist.model.dynamodb.UserTableKeyPrefix
import com.example.ipwhitelist.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun createUser(createUserRequest: CreateUserRequest): UserResponse? {
        val userPrincipal = createUserRequest.toModel()
        println("Creating user: $userPrincipal")
        return userRepository.save(userPrincipal)?.toResponse()
    }

    fun findByEmail(email: String): UserResponse? {
        return userRepository.findUserPrincipalByEmail(email)?.toResponse()
    }

    fun findById(id: UUID): UserResponse? {
        return userRepository.findUserPrincipalByUserId(id.toUserId())?.toResponse()
    }

    fun deleteById(id: UUID): Boolean {
        this.findById(id) ?: return false
        userRepository.deleteByUserId(id.toUserId())
        return true
    }

    private fun CreateUserRequest.toModel(): UserPrincipal {
        val id = UUID.randomUUID()
        return UserPrincipal(
            userId = "${UserTableKeyPrefix.USER.prefix}$id",
            objectId = "${UserTableKeyPrefix.USER.prefix}$id",
            email = this.email,
            role = "USER"
        )
    }

    private fun UUID.toUserId() = "${UserTableKeyPrefix.USER.prefix}$this"

    private fun UserPrincipal.toResponse() = UserResponse(
        id = this.userId.substringAfter(UserTableKeyPrefix.USER.prefix),
        email = this.email,
        role = this.role
    )
}