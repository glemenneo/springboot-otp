package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.OtpRequest
import com.example.ipwhitelist.model.VerifyOtpRequest
import com.example.ipwhitelist.model.VerifyOtpResponse
import com.example.ipwhitelist.service.AuthService
import com.example.ipwhitelist.service.JwtService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.InternalAuthenticationServiceException
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
            return ResponseEntity.ok("OTP sent to ${otpRequest.email}")
        } catch (error: Exception) {
            return ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/verify-otp")
    fun verifyOtp(@RequestBody verifyOtpRequest: VerifyOtpRequest): ResponseEntity<VerifyOtpResponse> {
        try {
            authService.verifyOtp(verifyOtpRequest.email, verifyOtpRequest.otp)
            return ResponseEntity.ok(VerifyOtpResponse(token = "some jwt"))
        } catch (error: NoSuchElementException) {
            return ResponseEntity.notFound().build()
        } catch (error: InternalAuthenticationServiceException) {
            return ResponseEntity.badRequest().build()
        }
    }
}