package com.example.ipwhitelist.config

import com.example.ipwhitelist.service.JwtService
import com.example.ipwhitelist.service.UserDetailsService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    val userDetailsService: UserDetailsService,
    val jwtService: JwtService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authToken = request.getHeader("Authorization")?.substringAfter("Bearer ")
        //TODO implement extraction from cookies

        if (authToken == null) {
            filterChain.doFilter(request, response)
            return
        }

        val email = jwtService.extractEmail(authToken)
        if (email == null) {
            filterChain.doFilter(request, response)
            return
        }

        val userDetails = userDetailsService.loadUserByUsername(email)

        val isValid = jwtService.isValid(authToken, userDetails)
        if (!isValid) {
            filterChain.doFilter(request, response)
            return
        }

        val authenticatedToken =
            UsernamePasswordAuthenticationToken(userDetails.username, null, userDetails.authorities)
        authenticatedToken.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authenticatedToken

        filterChain.doFilter(request, response)
    }
}