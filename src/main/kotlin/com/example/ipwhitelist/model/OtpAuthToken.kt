package com.example.ipwhitelist.model

import org.springframework.security.authentication.AbstractAuthenticationToken

class OtpAuthToken(
    val email: String,
    val otp: String,
): AbstractAuthenticationToken(null) {
    override fun getCredentials(): String {
        return otp
    }

    override fun getPrincipal(): String {
        return email
    }
}