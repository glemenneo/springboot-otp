package com.example.ipwhitelist.model

data class AddAppUserRequest(
    val email : String,
    val role : UserRole
)

enum class UserRole {
    Admin,
    User
}