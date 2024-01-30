package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.CreateUserRequest
import com.example.ipwhitelist.model.UserResponse
import com.example.ipwhitelist.model.dynamodb.User
import com.example.ipwhitelist.model.dynamodb.UserPrincipal
import com.example.ipwhitelist.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("api/v1/users")
class UserController(private val userService: UserService) {
    @PostMapping
    fun createUser(@RequestBody createUserRequest: CreateUserRequest): ResponseEntity<UserResponse> {
        val userEntity = userService.createUser(createUserRequest)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create User, User already exists!")

        val location =
            ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(userEntity.userId).toUri()
        return ResponseEntity.created(location).body(userEntity.toResponse())
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String): ResponseEntity<UserResponse> {
        val userEntity = userService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!")
        return ResponseEntity.ok().body(userEntity.toResponse())
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: String): ResponseEntity<Unit> {
        val isDeleted = userService.deleteById(id)

        return if (isDeleted) ResponseEntity.noContent().build()
        else throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!")
    }

    private fun User.toResponse() = when (this) {
        is UserPrincipal -> UserResponse(
            id = this.userId,
            email = this.email,
            role = this.role
        )
        else -> throw IllegalArgumentException("Unexpected type of User: $this")
    }
}