package com.example.ipwhitelist.repository

import com.example.ipwhitelist.model.Otp
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OtpRepository : CrudRepository<Otp, String> {
    fun save(otp: Otp): Otp
    fun findByEmailAndUserAgent(email: String, userAgent: String): Otp?
}