package com.example.ipwhitelist.service

import com.example.ipwhitelist.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService(
    private val jwtProperties: JwtProperties
) {
    private val jwtSecret = Keys.hmacShaKeyFor(jwtProperties.key.toByteArray())

    fun generateToken(userDetails: UserDetails, expirationDate: Date, additionalClaims: Map<String, Any> = emptyMap()): String =
        Jwts.builder()
            .claims()
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(expirationDate)
            .add(additionalClaims)
            .and()
            .signWith(jwtSecret)
            .compact()

    fun isValid(token: String, userDetails: UserDetails): Boolean {
        val email = extractClaims(token).subject
        return userDetails.username == email && !isExpired(token)
    }

    fun isExpired(token: String): Boolean =
        extractClaims(token)
            .expiration
            .before(Date(System.currentTimeMillis()))

    fun extractEmail(token: String): String =
        extractClaims(token).subject

    private fun extractClaims(token: String): Claims {
        val parser = Jwts.parser()
            .verifyWith(jwtSecret)
            .build()

        return parser
            .parseSignedClaims(token)
            .payload
    }
}