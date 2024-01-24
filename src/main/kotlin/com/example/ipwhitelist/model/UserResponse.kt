package com.example.ipwhitelist.model

import java.util.UUID

data class UserResponse(
    val id: UUID,
    val name: String,
    val email: String,
    val role: String
)
