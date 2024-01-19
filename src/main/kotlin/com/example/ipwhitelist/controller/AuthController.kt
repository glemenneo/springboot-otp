package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.OtpRequest
import com.example.ipwhitelist.model.VerifyOtpRequest
import com.example.ipwhitelist.service.AuthService
import com.example.ipwhitelist.service.JwtService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtService: JwtService
) {

    @PostMapping("/request-otp")
    fun requestOtp(@RequestBody otpRequest: OtpRequest) {
        authService.requestOtp(otpRequest.email)
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