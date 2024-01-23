package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.OtpRequest
import com.example.ipwhitelist.model.VerifyOtpRequest
import com.example.ipwhitelist.model.VerifyOtpResponse
import com.example.ipwhitelist.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/request-otp")
    fun requestOtp(@RequestHeader(value = "User-Agent") userAgent: String, @RequestBody otpRequest: OtpRequest): ResponseEntity<String> {
        try {
            authService.requestOtp(userAgent ,otpRequest.email)
            return ResponseEntity.ok("OTP sent to ${otpRequest.email}")
        } catch (error: Exception) {
            return ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/verify-otp")
    fun verifyOtp(@RequestHeader(value = "User-Agent") userAgent: String, @RequestBody verifyOtpRequest: VerifyOtpRequest): ResponseEntity<VerifyOtpResponse> {
        println("API triggered")
        return ResponseEntity.ok(null)
    }
}