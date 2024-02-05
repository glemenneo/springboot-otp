package com.example.ipwhitelist.model

import com.example.ipwhitelist.model.dynamodb.UserRole

data class AddAppUserRequest(
    val email : String,
    val role : UserRole
)