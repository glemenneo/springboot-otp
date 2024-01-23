package com.example.ipwhitelist.model

data class CreateUserRequest(
    val name: String,
    val email: String,
    val role: String
)
