package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.OtpRequest
import com.example.ipwhitelist.model.VerifyOtpRequest
import com.example.ipwhitelist.model.VerifyOtpResponse
import com.example.ipwhitelist.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/request-otp")
    fun requestOtp(@RequestBody otpRequest: OtpRequest): ResponseEntity<String> {
        try {
            authService.requestOtp(otpRequest.email)
            return ResponseEntity.ok("OTP sent to ${otpRequest.email}")
        } catch (error: Exception) {
            error.printStackTrace()
            return ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/verify-otp")
    fun verifyOtp(@RequestBody verifyOtpRequest: VerifyOtpRequest): ResponseEntity<VerifyOtpResponse> {
        println("API triggered")
        val jwtToken = authService.verifyOtp(verifyOtpRequest)
            ?: return ResponseEntity.status(401).body(VerifyOtpResponse("Invalid OTP or OTP does not exist"))

        return ResponseEntity.ok(VerifyOtpResponse(jwtToken))
    }
}