package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.OtpRequest
import com.example.ipwhitelist.model.VerifyOtpRequest
import com.example.ipwhitelist.service.AuthService
import com.example.ipwhitelist.service.JwtService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/auth")
class AuthController(
    private val authService: AuthService,
    private val authenticationService: AuthService
) {

    @PostMapping("/request-otp")
    fun requestOtp(@RequestBody otpRequest: OtpRequest): ResponseEntity<String> {
        try {
            authService.requestOtp(otpRequest.email)
            return ResponseEntity<String>("OTP sent to ${otpRequest.email}", HttpStatus.OK)
        } catch (error: Exception) {
            return ResponseEntity<String>("Error sending OTP to ${otpRequest.email}", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping("/verify-otp")
    fun verifyOtp(@RequestBody verifyOtpRequest: VerifyOtpRequest) {
        val isValid = authService.verifyOtp(verifyOtpRequest.email, verifyOtpRequest.otp)
//        if (isValid) {
//            val token = jwtService.generateToken(verifyOtpRequest.email)
//        } else {
//
//        }
    }
}