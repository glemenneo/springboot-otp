package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.CreateUserRequest
import com.example.ipwhitelist.model.UserResponse
import com.example.ipwhitelist.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*

@RestController
@RequestMapping("api/v1/users")
class UserController(private val userService: UserService) {
    @PostMapping
    fun createUser(@RequestBody createUserRequest: CreateUserRequest): ResponseEntity<UserResponse> {
        val userResponse = userService.createUser(createUserRequest)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create User, User already exists!")
        val location =
            ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(userResponse.id).toUri()
        return ResponseEntity.created(location).body(userResponse)
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val userResponse = userService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!")
        return ResponseEntity.ok().body(userResponse)
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: UUID): ResponseEntity<Unit> {
        val isDeleted = userService.deleteById(id)

        return if (isDeleted) ResponseEntity.noContent().build()
        else throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!")
    }
}