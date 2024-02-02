package com.example.ipwhitelist.service

import com.example.ipwhitelist.model.*
import com.example.ipwhitelist.model.dynamodb.ApplicationUser
import com.example.ipwhitelist.model.dynamodb.AppTableKeyPrefix
import com.example.ipwhitelist.model.dynamodb.ApplicationDetails
import com.example.ipwhitelist.repository.AppRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AppService(
    private val appRepository: AppRepository,
    private val userService: UserService
) {

    fun getAppsByUserId(userId : String) : List<EnhancedAppResponse> {
        return appRepository.getAppsByUserId(userId)
    }

    fun findById(id: UUID) : ApplicationDetails? {
        return appRepository.findAppDetailsByAppId(id.toString())
    }

    fun findAdminsByAppId(id: UUID) : List<String> {
        return appRepository.findAdminsByAppId(id.toString())
    }

    fun findUsersByAppId(id: UUID) : List<String> {
        return appRepository.findUsersByAppId(id.toString())
    }

    fun createApp(createAppRequest: CreateAppRequest) : ApplicationDetails? {
        val appEntity = createAppRequest.toModel()
        println("Creating app: $appEntity")
        appRepository.save(appEntity)
        return appEntity
    }

    fun deleteApp(appId: String) {
        appRepository.deleteApp(appId)
    }

    fun addUser(appId : String, addAppUserRequest: AddAppUserRequest) {
        // fetch existing user, else create new user with the specified role
        val user = userService.findByEmail(addAppUserRequest.email)
            ?: userService.createUser(CreateUserRequest(addAppUserRequest.email, addAppUserRequest.role.toString()
        ))
        val appUser = ApplicationUser(
            appId = AppTableKeyPrefix.APP.prefix + appId,
            objectId = user!!.userId,
            role = addAppUserRequest.role.toString(),
        )
        appRepository.save(appUser)
    }

    fun deleteUser(appId: String, deleteAppUserRequest: DeleteAppUserRequest) {
        appRepository.deleteUser(appId, deleteAppUserRequest.userId)
    }

    private fun CreateAppRequest.toModel() = ApplicationDetails(
        appId = AppTableKeyPrefix.APP.prefix + UUID.randomUUID().toString(),
        objectId = AppTableKeyPrefix.APP.prefix + UUID.randomUUID().toString(),
        name = this.name,
        description = this.description,
        accountId = this.accountId,
        changeToken = this.changeToken,
        ipSetId = this.ipSetId
    )

}