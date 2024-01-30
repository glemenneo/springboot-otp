package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.*
import com.example.ipwhitelist.model.dynamodb.Application
import com.example.ipwhitelist.model.dynamodb.ApplicationDetails
import com.example.ipwhitelist.service.AppService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*

@RestController
@RequestMapping("api/v1/apps")
class AppController(
    private val appService : AppService
) {

    @GetMapping
    fun getAllApps(): ResponseEntity<List<AppResponse>> {
        val apps = appService.getAllApps()
        val appResponses = apps.map {
            AppResponse(
                id = it.appId,
                name = it.name,
                description = it.description
            )
        }
        return ResponseEntity.ok(appResponses)
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<AppResponse> {
        val entity = appService.findById(id)

        val appResponse = entity?.toResponse()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Application with ID $id not found")

        return ResponseEntity.ok().body(appResponse)
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    fun createApp(@RequestBody createAppRequest: CreateAppRequest) : ResponseEntity<AppResponse> {
        val appEntity = appService.createApp(createAppRequest)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create application, application already exists!")

        val location =
            ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(appEntity.appId).toUri()
        return ResponseEntity.created(location).body(appEntity.toResponse())
    }

    @DeleteMapping("/{appId}")
    fun deleteApp(@PathVariable appId: String) {
        appService.deleteApp(appId)
    }

    @PostMapping("/{appId}/users")
    fun addUser(@PathVariable appId: String, @RequestBody addAppUserRequest: AddAppUserRequest) : ResponseEntity<Unit> {
        appService.addUser(appId, addAppUserRequest)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/{appId}/users")
    fun deleteUser(@PathVariable appId: String, @RequestBody deleteAppUserRequest: DeleteAppUserRequest) : ResponseEntity<Unit> {
        appService.deleteUser(appId, deleteAppUserRequest)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    private fun Application.toResponse() = when (this) {
        is ApplicationDetails -> AppResponse(
            id = this.appId,
            name = this.name,
            description = this.description
        )
        else -> throw IllegalArgumentException("Unexpected type of application: $this")
    }

}