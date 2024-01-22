package com.example.ipwhitelist.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity()
class SecurityConfig {
    @Bean
    fun filterChain(
        http: HttpSecurity,
        jwtAuthFilter: JwtAuthFilter,

    ): DefaultSecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests{
                it
                .requestMatchers("/api/auth/request-otp", "/api/auth/verify-otp")
                .permitAll()
                .anyRequest()
                .fullyAuthenticated()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}