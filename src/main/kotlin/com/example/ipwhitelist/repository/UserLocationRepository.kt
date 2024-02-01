package com.example.ipwhitelist.repository

import com.example.ipwhitelist.model.dynamodb.UserIp
import com.example.ipwhitelist.model.dynamodb.UserLocation
import com.example.ipwhitelist.model.dynamodb.UserTableKeyPrefix
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.*
import software.amazon.awssdk.enhanced.dynamodb.model.*
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException
import java.util.stream.Collectors

@Repository
class UserLocationRepository(
    val dynamoDbEnhancedClient: DynamoDbEnhancedClient
) {
    fun findUserLocationByLocationId(userKey: String, locationKey: String): UserLocation? {
        return getUserLocationTable()
            .getItem(
                Key.builder()
                    .partitionValue(userKey)
                    .sortValue(locationKey)
                    .build()
            )
    }

    fun findUserIpByLocationId(userKey: String, locationKey: String): UserIp? {
        return getUserIpTable()
            .getItem(
                Key.builder()
                    .partitionValue(userKey)
                    .sortValue("${UserTableKeyPrefix.IP.prefix}${locationKey.substringAfter(UserTableKeyPrefix.LOCATION.prefix)}")
                    .build()
            )
    }

    fun saveLocationAndIp(userLocation: UserLocation, userIp: UserIp): Boolean {
        try {
            dynamoDbEnhancedClient.transactWriteItems {
                it
                    .addPutItem(getUserLocationTable(), userLocation)
                    .addPutItem(getUserIpTable(), userIp)
                    .build()
            }
            return true
        } catch (ex: TransactionCanceledException) {
            ex.cancellationReasons().stream().forEach {
                println(it.toString())
            }
            return false
        }
    }

    fun updateLocationAndIp(userLocation: UserLocation, userIp: UserIp): Boolean {
        try {
            dynamoDbEnhancedClient.transactWriteItems {
                it.addUpdateItem(
                    getUserLocationTable(), TransactUpdateItemEnhancedRequest.builder(UserLocation::class.java)
                        .item(userLocation)
                        .build()
                ).addUpdateItem(
                    getUserIpTable(), TransactUpdateItemEnhancedRequest.builder(UserIp::class.java)
                        .item(userIp)
                        .build()
                ).build()
            }
            return true
        } catch (ex: TransactionCanceledException) {
            ex.cancellationReasons().stream().forEach {
                println(it.toString())
            }
            return false
        }
    }

    fun findUserLocationsByUserId(userKey: String): List<UserLocation> {
        val userLocationTable = getUserLocationTable()
        val queryConditional = QueryConditional.sortBeginsWith(
            Key.builder()
                .partitionValue(userKey)
                .sortValue(UserTableKeyPrefix.LOCATION.prefix)
                .build()
        )
        return userLocationTable
            .query(queryConditional)
            .items()
            .stream()
            .collect(Collectors.toList())
    }

    fun findUserIpsByLocationIds(userKey: String, locationKeys: List<String>): List<UserIp> {
        val userIpReadBatch = ReadBatch.builder(UserIp::class.java)
        locationKeys.forEach {
            userIpReadBatch.addGetItem(
                Key.builder()
                    .partitionValue(userKey)
                    .sortValue("${UserTableKeyPrefix.IP.prefix}${it.substringAfter(UserTableKeyPrefix.LOCATION.prefix)}")
                    .build()
            )
        }

        val resultPages = dynamoDbEnhancedClient.batchGetItem {
            it.readBatches(userIpReadBatch.build())
        }

        return resultPages.resultsForTable(getUserIpTable())
            .stream()
            .collect(Collectors.toList())
    }

    fun deleteByLocationId(userKey: String, locationKey: String): Boolean {
        val ipKey = "${UserTableKeyPrefix.IP.prefix}${locationKey.substringAfter(UserTableKeyPrefix.LOCATION.prefix)}"
        try {
            dynamoDbEnhancedClient.transactWriteItems {
                it.addDeleteItem(
                    getUserLocationTable(),
                    Key.builder()
                        .partitionValue(userKey)
                        .sortValue(locationKey)
                        .build()
                ).addDeleteItem(
                    getUserIpTable(),
                    Key.builder()
                        .partitionValue(userKey)
                        .sortValue(ipKey)
                        .build()
                ).build()
            }
            return true
        } catch (ex: TransactionCanceledException) {
            ex.cancellationReasons().stream().forEach {
                println(it.toString())
            }
            return false
        }
    }

    fun deleteUserIpByLocationId(userKey: String, locationKey: String): Boolean {
        val ipKey = "${UserTableKeyPrefix.IP.prefix}${locationKey.substringAfter(UserTableKeyPrefix.LOCATION.prefix)}"
        getUserIpTable().deleteItem(
            Key.builder()
                .partitionValue(userKey)
                .sortValue(ipKey)
                .build()
        )

        return true
    }

    private fun getUserLocationTable(): DynamoDbTable<UserLocation> {
        return dynamoDbEnhancedClient.table("Users", TableSchema.fromBean(UserLocation::class.java))
    }

    private fun getUserIpTable(): DynamoDbTable<UserIp> {
        return dynamoDbEnhancedClient.table("Users", TableSchema.fromBean(UserIp::class.java))
    }
}