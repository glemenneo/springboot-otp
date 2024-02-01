package com.example.ipwhitelist.repository

import com.example.ipwhitelist.model.dynamodb.UserIp
import com.example.ipwhitelist.model.dynamodb.UserLocation
import com.example.ipwhitelist.model.dynamodb.UserTableKeyPrefix
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch
import java.util.stream.Collectors

@Repository
class UserLocationRepository(
    val dynamoDbEnhancedClient: DynamoDbEnhancedClient
) {
    fun findUserLocationsByUserId(userId: String): List<UserLocation> {
        val userLocationTable = getUserLocationTable()
        val queryConditional = QueryConditional.sortBeginsWith(
            Key.builder()
                .partitionValue(userId)
                .sortValue(UserTableKeyPrefix.LOCATION.prefix)
                .build()
        )
        return userLocationTable
            .query(queryConditional)
            .items()
            .stream()
            .collect(Collectors.toList())
    }

    fun findUserIpsByLocationIds(userId: String, locationIds: List<String>): List<UserIp> {
        val userIpReadBatch = ReadBatch.builder(UserIp::class.java)
        locationIds.forEach {
            userIpReadBatch.addGetItem(
                Key.builder()
                    .partitionValue(userId)
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

    private fun getUserLocationTable(): DynamoDbTable<UserLocation> {
        return dynamoDbEnhancedClient.table("Users", TableSchema.fromBean(UserLocation::class.java))
    }
    private fun getUserIpTable(): DynamoDbTable<UserIp> {
        return dynamoDbEnhancedClient.table("Users", TableSchema.fromBean(UserIp::class.java))
    }
}