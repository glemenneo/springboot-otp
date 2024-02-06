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
        return userRepository.findUserPrincipalByUserId(id.toUserKey())?.toResponse()
    }

    fun deleteById(id: UUID): Boolean {
        this.findById(id) ?: return false
        return userRepository.deleteByUserId(id.toUserKey())
    }

    private fun CreateUserRequest.toModel(): UserPrincipal {
        val id = UUID.randomUUID()
        return UserPrincipal(
            userId = id.toUserKey(), objectId = id.toUserKey(), email = this.email, role = "USER"
        )
    }

    private fun UUID.toUserKey() = "${UserTableKeyPrefix.USER.prefix}$this"

    private fun String.fromKey(keyPrefix: UserTableKeyPrefix) = UUID.fromString(substringAfter(keyPrefix.prefix))

    private fun UserPrincipal.toResponse() = UserResponse(
        id = this.userId.fromKey(UserTableKeyPrefix.USER), email = this.email, role = this.role
    )
}