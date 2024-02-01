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
    fun createLocation(userId: UUID, updateLocationRequest: UpdateLocationRequest): UserLocationResponse {

    }

    fun findByUserId(userId: UUID): UserLocationsResponse {
        val userLocations = userLocationRepository.findUserLocationsByUserId(userId.toUserId())
        val userIps = userLocationRepository.findUserIpsByLocationIds(
            userId.toUserId(),
            userLocations.map { it.objectId }
        )

        return UserLocationsResponse(
            locations = userLocations.map { userLocation ->
                val userIp = userIps.find {
                    it.objectId.substringAfter(UserTableKeyPrefix.IP.prefix) == userLocation.objectId.substringAfter(
                        UserTableKeyPrefix.LOCATION.prefix
                    )
                }
                userLocation.toResponse(userIp)
            }
        )
    }

    fun updateByUserId(userId: UUID, updateLocationRequest: UpdateLocationRequest): Boolean {

    }

    fun deleteById() {}

    fun deleteIpById() {}

    private fun UUID.toUserId() = "${UserTableKeyPrefix.USER.prefix}$this"

    private fun UserLocation.toResponse(userIp: UserIp?) = UserLocationResponse(
        id = UUID.fromString(this.objectId.substringAfter(UserTableKeyPrefix.LOCATION.prefix)),
        name = this.location,
        ip = userIp?.ip,
        ttl = userIp?.ttl
    )
}