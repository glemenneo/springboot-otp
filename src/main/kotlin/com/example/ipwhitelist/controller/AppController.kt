package com.example.ipwhitelist.controller

import com.example.ipwhitelist.model.*
import com.example.ipwhitelist.model.dynamodb.*
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
        val userId = userService.findByEmail(authentication.name)!!.id
        val userKey = "${UserTableKeyPrefix.USER.prefix}$userId"
        val userApps = appService.getAppsByUserKey(userKey)

        return ResponseEntity.ok(userApps)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('${UserRole.Constants.ADMIN_ROLE}')")
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
    @PreAuthorize("hasRole('${UserRole.Constants.ADMIN_ROLE}')")
    fun createApp(@RequestBody createAppRequest: CreateAppRequest) : ResponseEntity<CreateAppResponse> {
        val appEntity = appService.createApp(createAppRequest)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create application, application already exists!")

        val location =
            ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(appEntity.appId).toUri()
        return ResponseEntity.created(location).body(appEntity.toResponse())
    }

    @DeleteMapping("/{appId}")
    @PreAuthorize("hasRole('${UserRole.Constants.ADMIN_ROLE}')")
    fun deleteApp(@PathVariable appId: UUID) : ResponseEntity<Unit> {
        val isDeleted = appService.deleteById(appId)
        if (!isDeleted) {
            return ResponseEntity.badRequest().build()
        }
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{appId}/users")
    @PreAuthorize("hasRole('${UserRole.Constants.ADMIN_ROLE}')")
    fun addUser(@PathVariable appId: UUID, @RequestBody addAppUserRequest: AddAppUserRequest) : ResponseEntity<Unit> {
        appService.addUser(appId, addAppUserRequest)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/{appId}/users")
    @PreAuthorize("hasRole('${UserRole.Constants.ADMIN_ROLE}')")
    fun deleteUser(@PathVariable appId: UUID, @RequestBody deleteAppUserRequest: DeleteAppUserRequest) : ResponseEntity<Unit> {
        val isDeleted = appService.deleteUser(appId, deleteAppUserRequest)
        if (!isDeleted) {
            return ResponseEntity.badRequest().build()
        }
        return ResponseEntity.noContent().build()
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