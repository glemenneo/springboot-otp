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

        return UserLocationResponse(
            id = locationId,
            name = userLocation.location,
            ip = userIp.ip,
            ttl = userIp.ttl,
        )
    }

    fun findByUserId(userId: UUID): UserLocationsResponse {
        val userLocations = userLocationRepository.findUserLocationsByUserId(userId.toKey(UserTableKeyPrefix.USER))
        val userIps = userLocationRepository.findUserIpsByLocationIds(
            userId.toKey(UserTableKeyPrefix.USER),
            userLocations.map { it.objectId }
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

        return userLocationRepository.updateLocationAndIp(
            updateLocationRequest.toLocationModel(userId, updateLocationRequest.id),
            updateLocationRequest.toIpModel(userId, updateLocationRequest.id)
        )
    }

    fun deleteById() {}

    fun deleteIpById() {}

    private fun UUID.toKey(keyPrefix: UserTableKeyPrefix) = "${keyPrefix}$this"

    private fun String.fromKey(keyPrefix: UserTableKeyPrefix) = UUID.fromString(this.substringAfter(keyPrefix.prefix))

    private fun UpdateLocationRequest.toLocationModel(userId: UUID, locationId: UUID) = UserLocation(
        userId = userId.toKey(UserTableKeyPrefix.USER),
        objectId = locationId.toKey(UserTableKeyPrefix.LOCATION),
        location = this.name
    )

    private fun UpdateLocationRequest.toIpModel(userId: UUID, locationId: UUID) = UserIp(
        userId = userId.toKey(UserTableKeyPrefix.USER),
        objectId = locationId.toKey(UserTableKeyPrefix.LOCATION),
        ip = this.ip,
        ttl = 86400
    )

    private fun UserLocation.toResponse(userIp: UserIp?) = UserLocationResponse(
        id = this.objectId.fromKey(UserTableKeyPrefix.LOCATION),
        name = this.location,
        ip = userIp?.ip,
        ttl = userIp?.ttl
    )
}