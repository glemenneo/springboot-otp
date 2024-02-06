package com.example.ipwhitelist.model

import java.util.UUID

data class UpdateLocationRequest(
    val id: UUID?,
    val name: String,
    val ip: String,
)
