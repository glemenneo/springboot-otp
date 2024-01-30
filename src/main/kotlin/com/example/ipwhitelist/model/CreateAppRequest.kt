package com.example.ipwhitelist.model

data class CreateAppRequest (
    val name: String,
    val description: String,
    val accountId: String,
    val changeToken: String,
    val ipSetId: String
)