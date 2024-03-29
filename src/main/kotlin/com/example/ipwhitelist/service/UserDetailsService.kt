package com.example.ipwhitelist.service

import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsService(
    private val userService: UserService
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val userEntity = userService.findByEmail(username)
            ?: throw UsernameNotFoundException("User with email $username not found.")

        return User
            .withUsername(userEntity.email)
            .password("")
            .roles(userEntity.role)
            .build()
    }
}