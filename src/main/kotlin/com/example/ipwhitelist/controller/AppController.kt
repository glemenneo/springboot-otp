package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.*
import com.example.ipwhitelist.model.dynamodb.Application
import com.example.ipwhitelist.model.dynamodb.ApplicationDetails
import com.example.ipwhitelist.service.AppService
import com.example.ipwhitelist.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*

@RestController
@RequestMapping("api/v1/apps")
class AppController(
    private val appService : AppService,
    private val userService: UserService
) {

    @GetMapping
    fun findAppsByUserId(): ResponseEntity<List<EnhancedAppResponse>> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userPrincipal = userService.findByEmail(authentication.name)

        val userApps = appService.getAppsByUserId(userPrincipal!!.userId)

        return ResponseEntity.ok(userApps)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun findById(@PathVariable id: UUID): ResponseEntity<AppResponse> {
        val appDetails = appService.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Application with ID $id not found")
        val appAdmins = appService.findAdminsByAppId(id)
        val appUsers = appService.findUsersByAppId(id)

        //TODO: map userIds back to emails
        return ResponseEntity.ok(
            AppResponse(
                appDetails.appId,
                appDetails.name,
                appDetails.description,
                appAdmins,
                appUsers
            )
        )
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createApp(@RequestBody createAppRequest: CreateAppRequest) : ResponseEntity<CreateAppResponse> {
        val appEntity = appService.createApp(createAppRequest)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create application, application already exists!")

        val location =
            ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(appEntity.appId).toUri()
        return ResponseEntity.created(location).body(appEntity.toResponse())
    }

    @DeleteMapping("/{appId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteApp(@PathVariable appId: String) {
        appService.deleteApp(appId)
    }

    @PostMapping("/{appId}/users")
    @PreAuthorize("hasRole('ADMIN')")
    fun addUser(@PathVariable appId: String, @RequestBody addAppUserRequest: AddAppUserRequest) : ResponseEntity<Unit> {
        appService.addUser(appId, addAppUserRequest)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/{appId}/users")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(@PathVariable appId: String, @RequestBody deleteAppUserRequest: DeleteAppUserRequest) : ResponseEntity<Unit> {
        appService.deleteUser(appId, deleteAppUserRequest)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    private fun Application.toResponse() = when (this) {
        is ApplicationDetails -> CreateAppResponse(
            id = this.appId,
            name = this.name,
            description = this.description
        )
        else -> throw IllegalArgumentException("Unexpected type of application: $this")
    }

}