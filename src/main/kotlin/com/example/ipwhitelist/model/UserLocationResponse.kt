package com.example.ipwhitelist.model

import java.util.UUID

data class UserLocationResponse(
    val id: UUID,
    val name: String,
    val ip: String?,
    val ttl: Long?
)
