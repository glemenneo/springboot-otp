package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.OtpRequest
import com.example.ipwhitelist.model.VerifyOtpRequest
import com.example.ipwhitelist.model.VerifyOtpResponse
import com.example.ipwhitelist.service.AuthService
import com.example.ipwhitelist.service.JwtService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtService : JwtService
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
        val isOtpValid = authService.verifyOtp(verifyOtpRequest.email, verifyOtpRequest.otp)

        if (isOtpValid) {
            val expirationDate = Date(System.currentTimeMillis() + 30 * 60 * 1000)
            val jwtToken = jwtService.generateToken(verifyOtpRequest.email, expirationDate)

            return ResponseEntity.ok(VerifyOtpResponse(jwtToken))
        } else {
            return ResponseEntity.status(401).body(VerifyOtpResponse("Invalid OTP"))
        }
    }
}