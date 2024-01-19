package com.example.ipwhitelist.model

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.util.UUID

@RedisHash("otp")
data class Otp (
    @Id
    val id: UUID,
    val email: String,
    val otp: String,
    @TimeToLive
    val expirationTime: Long
)