package com.example.ipwhitelist.model

data class AppResponse(
    val id: String,
    val name: String,
    val description: String,
    val admins: List<String>,
    val users: List<String>,
)
