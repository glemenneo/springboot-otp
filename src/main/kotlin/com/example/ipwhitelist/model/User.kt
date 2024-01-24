package com.example.ipwhitelist.model

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.util.UUID

@RedisHash("user")
data class User(
    @Id
    val id: UUID,
    val name: String,
    @Indexed
    val email: String,
    val role: String
)
