package com.example.ipwhitelist.config

import com.example.ipwhitelist.model.dynamodb.UserRole
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity()
@EnableMethodSecurity
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
            .authorizeHttpRequests {
                it
                    .requestMatchers("/api/v1/auth/request-otp", "/api/v1/auth/verify-otp")
                    .permitAll()
                    .requestMatchers("/api/v1/users/**")
                    .permitAll()
                    .requestMatchers("/api/v1/apps/**")
                    .permitAll()
                    .requestMatchers("/api/v1/admin/*")
                    .hasRole(UserRole.ADMIN.name)
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