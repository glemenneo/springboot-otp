package com.example.ipwhitelist.config

import com.example.ipwhitelist.model.OtpAuthToken
import com.example.ipwhitelist.model.VerifyOtpRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component

@Component
class OtpAuthProcessingFilter
    : AbstractAuthenticationProcessingFilter(AntPathRequestMatcher("/api/auth/verify-otp", "POST")) {
    @Autowired
    override fun setAuthenticationManager(authenticationManager: AuthenticationManager) {
        super.setAuthenticationManager(authenticationManager)
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val userAgent = request.getHeader("User-Agent")
        val body = request.reader.use { it.readText() }
        val verifyOtpRequest: VerifyOtpRequest? = jacksonObjectMapper().readValue(body, VerifyOtpRequest::class.java)

        val email = verifyOtpRequest?.email
        val otp = verifyOtpRequest?.otp

        if (email == null || otp == null) {
            throw BadCredentialsException("Authentication details not present!")
        }

        val authentication = OtpAuthToken(email, otp, userAgent)
        return this.authenticationManager.authenticate(authentication)
    }
}