package com.example.ipwhitelist.repository

import com.example.ipwhitelist.model.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : CrudRepository<User, String> {
    fun save(user: User): User
    fun findByEmail(email: String): User?
    fun findByUuid(id: UUID): User?
    fun deleteByUuid(id: UUID): Boolean
}