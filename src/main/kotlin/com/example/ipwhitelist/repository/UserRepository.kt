package com.example.ipwhitelist.repository

import com.example.ipwhitelist.model.User
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepository {

    private val users = mutableListOf(
        User(id= UUID.randomUUID(), name="John", email="john@gmail.com"),
        User(id= UUID.randomUUID(), name="Jack", email="jack@gmail.com"),
        User(id= UUID.randomUUID(), name="James", email="james@gmail.com"),
    )

    fun save(user: User): Boolean = users.add(user)

    fun findByEmail(email: String): User? = users.find { it.email == email }

    fun findByUuid(id: UUID): User? = users.find { it.id == id }

    fun findAll(): Collection<User> = users

    fun deleteByUuid(id: UUID): Boolean = users.removeIf { it.id == id }
}