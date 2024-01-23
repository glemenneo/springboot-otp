package com.example.ipwhitelist.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity()
class SecurityConfig(
    val otpAuthProvider: OtpAuthProvider
) {
    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun filterChain(
        http: HttpSecurity,
        otpAuthProcessingFilter: OtpAuthProcessingFilter,
        jwtAuthFilter: JwtAuthFilter
    ): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests{
                it
                .requestMatchers("/api/auth/request-otp", "/api/auth/verify-otp")
                .permitAll()
                .requestMatchers("/api/admin/*")
                .hasRole("Admin")
                .anyRequest()
                .authenticated()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authenticationProvider(otpAuthProvider)
            .addFilterBefore(otpAuthProcessingFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(jwtAuthFilter, OtpAuthProcessingFilter::class.java)
            .build()
    }
}