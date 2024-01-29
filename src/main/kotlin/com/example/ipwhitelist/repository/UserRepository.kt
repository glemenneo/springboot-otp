package com.example.ipwhitelist.repository

import com.example.ipwhitelist.model.dynamodb.User
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository

@EnableScan
interface UserRepository : CrudRepository<User, String> {
    fun save(user: User): User
    fun findByEmail(email: String): User?
    fun findByUserIdAndEmail(userId: String, email: String): User?
    fun findUserByUserId(userId: String): User?
    fun deleteByUserId(userId: String)
}