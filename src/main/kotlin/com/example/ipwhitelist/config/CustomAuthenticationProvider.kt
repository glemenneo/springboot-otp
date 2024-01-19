package com.example.ipwhitelist.config

import com.example.ipwhitelist.repository.UserRepository
import com.example.ipwhitelist.service.AuthService
import com.example.ipwhitelist.service.CustomUserDetailsService
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationProvider(
    private val authService: AuthService,
    private val userDetailsService: CustomUserDetailsService
): AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val email = authentication.name
        val otp = authentication.credentials.toString()
        val isOtpValid = authService.verifyOtp(email, otp)
        if (isOtpValid) {
            val userDetails = userDetailsService.loadUserByUsername(email)
            return UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        } else {
            throw BadCredentialsException("Invalid OTP")
        }
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication?.equals(UsernamePasswordAuthenticationToken::class.java) ?: false
    }
}