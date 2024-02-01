package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.*
import com.example.ipwhitelist.service.UserLocationService
import com.example.ipwhitelist.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import java.util.*

@RestController
@RequestMapping("api/v1/users")
class UserController(
    private val userService: UserService,
    private val userLocationService: UserLocationService
) {
    @GetMapping("/{id}/locations")
    fun findLocations(@PathVariable id: UUID): ResponseEntity<UserLocationsResponse> {
        val userLocationsResponse = userLocationService.findByUserId(id)
        return ResponseEntity.ok().body(userLocationsResponse)
    }

    @PutMapping("/{id}/locations")
    fun updateLocations(
        @PathVariable id: UUID,
        @RequestBody updateLocationRequest: UpdateLocationRequest
    ): ResponseEntity<UserLocationResponse> {
        if (updateLocationRequest.id == null) {
            val userLocationResponse = userLocationService.createLocation(id, updateLocationRequest)
                ?: return ResponseEntity.badRequest().build()

            val location =
                ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(userLocationResponse.id).toUri()
            return ResponseEntity.created(location).build()
        }

        val isUpdated = userLocationService.updateByUserId(id, updateLocationRequest)
        if (!isUpdated) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{userId}/locations/{locationId}/ip")
    fun deleteIpByLocationId(@PathVariable userId: UUID, @PathVariable locationID: UUID): ResponseEntity<Unit> {
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{userId}/locations/{locationId}")
    fun deleteLocationByLocationId(@PathVariable userId: UUID, @PathVariable locationID: UUID): ResponseEntity<Unit> {
        return ResponseEntity.noContent().build()
    }

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