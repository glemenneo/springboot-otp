package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.User
import com.example.ipwhitelist.model.CreateUserRequest
import com.example.ipwhitelist.model.UserResponse
import com.example.ipwhitelist.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {
    @PostMapping
    fun createUser(@RequestBody createUserRequest: CreateUserRequest): ResponseEntity<UserResponse> {
        val userEntity = userService.createUser(createUserRequest)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create User, User already exists!")

        val location =
            ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(userEntity.id).toUri()
        return ResponseEntity.created(location).body(userEntity.toResponse())
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val userEntity = userService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!")
        return ResponseEntity.ok().body(userEntity.toResponse())
    }

    @DeleteMapping("/{id}")
    fun deleteById(@PathVariable id: UUID): ResponseEntity<Void> {
        val isDeleted = userService.deleteById(id)

        return if (isDeleted) ResponseEntity.noContent().build()
        else throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!")
    }

    private fun User.toResponse() = UserResponse(id = this.id, name = this.name, email = this.email, role = this.role)
}