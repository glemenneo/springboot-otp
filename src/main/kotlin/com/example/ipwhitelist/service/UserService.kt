package com.example.ipwhitelist.service

import com.example.ipwhitelist.model.User
import com.example.ipwhitelist.repository.UserRepository

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(private val userRepository: UserRepository) {

    fun createUser(user: User): User? {
        val duplicate = userRepository.findByEmail(user.email)

        if (duplicate != null) {
            return null
        } else {
            userRepository.save(user)
            return user
        }
    }

    fun findByEmail(email: String): User? = userRepository.findByEmail(email)

    fun findByUuid(id: UUID): User? = userRepository.findByUuid(id)

    fun findAll(): Collection<User> = userRepository.findAll()

    fun deleteByUuid(id: UUID): Boolean = userRepository.deleteByUuid(id)
}