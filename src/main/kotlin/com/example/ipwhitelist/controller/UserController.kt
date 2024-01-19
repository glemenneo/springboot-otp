package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.User
import com.example.ipwhitelist.model.UserRequest
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
import java.util.UUID

@RestController
@RequestMapping("api/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun createUser(@RequestBody user: UserRequest): User? = userService.createUser(user.toModel())
        ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create User, User already exists!")

    @GetMapping
    fun listAll(): Collection<User> = userService.findAll()

    @GetMapping("/{id}")
    fun findByUuid(@PathVariable id: UUID): User = userService.findByUuid(id)
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!")

    @DeleteMapping("/{id}")
    fun deleteByUuid(@PathVariable id: UUID): ResponseEntity<Boolean> {
        val isDeleted = userService.deleteByUuid(id)

        return if (isDeleted) ResponseEntity.noContent().build()
        else throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!")
    }

    private fun UserRequest.toModel() = User(id = UUID.randomUUID(), name = name, email = email)
}