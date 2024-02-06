package com.example.ipwhitelist.model

import java.util.UUID

data class UserResponse(
    val id: UUID,
    val email: String,
    val role: String
)
