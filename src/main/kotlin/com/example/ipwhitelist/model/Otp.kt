package com.example.ipwhitelist.model

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import org.springframework.data.redis.core.index.Indexed
import java.util.UUID


@RedisHash("otp")
data class Otp(
    @Id
    val id: UUID,
    @Indexed
    val email: String,
    val otp: String,
    @TimeToLive
    val expirationTime: Long
)