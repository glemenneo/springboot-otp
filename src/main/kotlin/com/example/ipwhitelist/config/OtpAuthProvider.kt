package com.example.ipwhitelist.config

import com.example.ipwhitelist.model.OtpAuthToken
import com.example.ipwhitelist.service.OtpService
import com.example.ipwhitelist.service.UserDetailsService
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class OtpAuthProvider(
    val userDetailsService: UserDetailsService,
    val otpService: OtpService
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        if (authentication !is OtpAuthToken) {
            throw IllegalArgumentException("Invalid authentication token.")
        }

        val email = authentication.principal
        val otp = authentication.credentials
        val userAgent = authentication.userAgent

        val isValid = otpService.validateOtp(userAgent, email, otp)
        if (!isValid) {
            throw BadCredentialsException("Invalid OTP.")
        }

        val userDetails = userDetailsService.loadUserByUsername(email)

        return UsernamePasswordAuthenticationToken(userDetails.username, null, userDetails.authorities)
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == OtpAuthToken::class.java
    }
}