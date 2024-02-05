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

    fun getAppsByUserKey(userKey : String) : List<EnhancedAppResponse> {
        return appRepository.getAppsByUserId(userKey)
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
        appRepository.save(appEntity)
        return appEntity
    }

    fun deleteById(appId: UUID) : Boolean {
        this.findById(appId) ?: return false

        val appKey = appId.toKey(AppTableKeyPrefix.APP)
        val isDeleted = appRepository.deleteByAppId(appKey)

        return isDeleted
    }

    fun addUser(appId : UUID, addAppUserRequest: AddAppUserRequest) {
        // fetch existing user, else create new user with the specified role
        val appKey = appId.toKey(AppTableKeyPrefix.APP)
        val user = userService.findByEmail(addAppUserRequest.email)
            ?: userService.createUser(CreateUserRequest(addAppUserRequest.email, addAppUserRequest.role.toString()
        ))
        val appUser = ApplicationUser(
            appId = appKey,
            objectId = user!!.userId,
            role = addAppUserRequest.role.toString(),
        )
        appRepository.save(appUser)
    }

    fun deleteUser(appId: UUID, deleteAppUserRequest: DeleteAppUserRequest) : Boolean {
        this.findById(appId) ?: return false

        val appKey = appId.toKey(AppTableKeyPrefix.APP)
        val userKey = deleteAppUserRequest.userId.toKey(AppTableKeyPrefix.USER)
        val isDeleted = appRepository.deleteUser(appKey, userKey)

        return isDeleted
    }

    private fun CreateAppRequest.toModel(): ApplicationDetails {
        val uuid = UUID.randomUUID().toString()

        return ApplicationDetails(
            appId = AppTableKeyPrefix.APP.prefix + uuid,
            objectId = AppTableKeyPrefix.APP.prefix + uuid,
            name = this.name,
            description = this.description,
            accountId = this.accountId,
            changeToken = this.changeToken,
            ipSetId = this.ipSetId
        )
    }

    private fun UUID.toKey(keyPrefix: AppTableKeyPrefix) = "${keyPrefix.prefix}$this"

}