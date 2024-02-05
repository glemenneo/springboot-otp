package com.example.ipwhitelist.service

import com.example.ipwhitelist.model.UpdateLocationRequest
import com.example.ipwhitelist.model.UserLocationResponse
import com.example.ipwhitelist.model.UserLocationsResponse
import com.example.ipwhitelist.model.dynamodb.UserIp
import com.example.ipwhitelist.model.dynamodb.UserLocation
import com.example.ipwhitelist.model.dynamodb.UserTableKeyPrefix
import com.example.ipwhitelist.repository.UserLocationRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserLocationService(
    val userLocationRepository: UserLocationRepository
) {
    fun createLocation(userId: UUID, updateLocationRequest: UpdateLocationRequest): UserLocationResponse? {
        val locationId = UUID.randomUUID()
        val userLocation = updateLocationRequest.toLocationModel(userId, locationId)
        val userIp = updateLocationRequest.toIpModel(userId, locationId)

        val existingUserLocations =
            userLocationRepository.findUserLocationsByUserId(userId.toKey(UserTableKeyPrefix.USER))
        if (existingUserLocations.size >= 2) {
            return null
        }

        val isCreated = userLocationRepository.saveLocationAndIp(userLocation, userIp)
        if (!isCreated) {
            return null
        }

        //TODO: Add IPSet add ip operation

        return UserLocationResponse(
            id = locationId,
            name = userLocation.name,
            ip = userIp.ip,
            ttl = userIp.ttl,
        )
    }

    fun findByUserId(userId: UUID): UserLocationsResponse {
        val userKey = userId.toKey(UserTableKeyPrefix.USER)

        val userLocations = userLocationRepository.findUserLocationsByUserId(userId.toKey(UserTableKeyPrefix.USER))

        val userLocationKeys = userLocations.map { it.objectId }
        val userIps = userLocationRepository.findUserIpsByLocationIds(
            userKey,
            userLocationKeys
        )

        return UserLocationsResponse(
            locations = userLocations.map { userLocation ->
                val userIp = userIps.find {
                    it.objectId.fromKey(UserTableKeyPrefix.IP) == userLocation.objectId.fromKey(UserTableKeyPrefix.LOCATION)
                }
                userLocation.toResponse(userIp)
            }
        )
    }

    fun updateByUserId(userId: UUID, updateLocationRequest: UpdateLocationRequest): Boolean {
        val userKey = userId.toKey(UserTableKeyPrefix.USER)
        val locationKey = updateLocationRequest.id!!.toKey(UserTableKeyPrefix.LOCATION)

        userLocationRepository.findUserLocationByLocationId(
            userKey,
            locationKey
        ) ?: return false

        val userLocation = updateLocationRequest.toLocationModel(userId, updateLocationRequest.id)
        val userIp = updateLocationRequest.toIpModel(userId, updateLocationRequest.id)

        val existingLocationIp = userLocationRepository.findUserIpByLocationId(userKey, locationKey)

        val isUpdated = userLocationRepository.updateLocationAndIp(userLocation, userIp)

        //TODO: Add IPSet add ip operation or add remove ip operation depending on if existing location ip exists

        return isUpdated
    }

    fun deleteById(userId: UUID, locationId: UUID): Boolean {
        val userKey = userId.toKey(UserTableKeyPrefix.USER)
        val locationKey = locationId.toKey(UserTableKeyPrefix.LOCATION)

        val existingLocationIp = userLocationRepository.findUserIpByLocationId(userKey, locationKey)
            ?: return userLocationRepository.deleteByLocationId(userKey, locationKey)

        val isDeleted = userLocationRepository.deleteByLocationId(userKey, locationKey)

        //TODO: Add IPSet remove ip operation

        return isDeleted
    }

    fun deleteIpById(userId: UUID, locationId: UUID): Boolean {
        val userKey = userId.toKey(UserTableKeyPrefix.USER)
        val locationKey = locationId.toKey(UserTableKeyPrefix.LOCATION)

        val isDeleted = userLocationRepository.deleteUserIpByLocationId(userKey, locationKey)

        //TODO: Add IPSet remove ip operation

        return isDeleted
    }

    private fun UUID.toKey(keyPrefix: UserTableKeyPrefix) = "${keyPrefix.prefix}$this"

    private fun String.fromKey(keyPrefix: UserTableKeyPrefix) = UUID.fromString(this.substringAfter(keyPrefix.prefix))

    private fun UpdateLocationRequest.toLocationModel(userId: UUID, locationId: UUID) = UserLocation(
        userId = userId.toKey(UserTableKeyPrefix.USER),
        objectId = locationId.toKey(UserTableKeyPrefix.LOCATION),
        name = this.name
    )

    private fun UpdateLocationRequest.toIpModel(userId: UUID, locationId: UUID) = UserIp(
        userId = userId.toKey(UserTableKeyPrefix.USER),
        objectId = locationId.toKey(UserTableKeyPrefix.LOCATION),
        ip = this.ip,
        ttl = 86400
    )

    private fun UserLocation.toResponse(userIp: UserIp?) = UserLocationResponse(
        id = this.objectId.fromKey(UserTableKeyPrefix.LOCATION),
        name = this.name,
        ip = userIp?.ip,
        ttl = userIp?.ttl
    )
}