package com.example.ipwhitelist.service

import com.example.ipwhitelist.model.CreateUserRequest
import com.example.ipwhitelist.model.User
import com.example.ipwhitelist.repository.UserRepository

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun createUser(createUserRequest: CreateUserRequest): User? {
        val duplicate = userRepository.findByEmail(createUserRequest.email)
        if (duplicate != null) {
            return null
        }

        val userEntity = createUserRequest.toModel()
        return userRepository.save(userEntity)

    }

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun findById(id: UUID): User? {
        return userRepository.findById(id)
    }

    fun deleteById(id: UUID): Boolean {
        this.findById(id) ?: return false

        userRepository.deleteById(id)
        return true
    }

    private fun CreateUserRequest.toModel() =
        User(id = UUID.randomUUID(), name = this.name, email = this.email, role = this.role)
}