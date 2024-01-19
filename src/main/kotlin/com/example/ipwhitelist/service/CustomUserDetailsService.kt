package com.example.ipwhitelist.service

import com.example.ipwhitelist.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.security.core.userdetails.User

typealias AppUser = com.example.ipwhitelist.model.User

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String) =
        userRepository.findByEmail(username)
            ?.mapToUserDetails()
            ?: throw UsernameNotFoundException("User not found with email: $username")

    private fun AppUser.mapToUserDetails() =
        User
            .withUsername(this.email)
            .build()
}