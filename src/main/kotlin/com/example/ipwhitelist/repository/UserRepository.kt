package com.example.ipwhitelist.repository

import com.example.ipwhitelist.model.dynamodb.*
import org.springframework.stereotype.Repository
import software.amazon.awssdk.core.pagination.sync.SdkIterable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.Page
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.sortBeginsWith
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch
import java.time.Instant

@Repository
class UserRepository(
    val dynamoDbEnhancedClient: DynamoDbEnhancedClient
) {
    fun <T : User> save(user: T): T {
        val userTable = getTable(user.javaClass)
        userTable.putItem(user)
        return user
    }

    fun findUserPrincipalByEmail(email: String): UserPrincipal? {
        val userTable = getTable(UserPrincipal::class.java)
        val emailGSI = userTable.index("EmailGSI")
        val queryConditional = QueryConditional.keyEqualTo {
            it.partitionValue(email)
        }
        val usersWithEmail: SdkIterable<Page<UserPrincipal>>? = emailGSI.query(
            QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build()
        )
        return usersWithEmail?.firstOrNull()?.items()?.firstOrNull()
    }

    fun findUserOtpByEmail(email: String): UserOtp? {
        val userId = findUserPrincipalByEmail(email)?.userId
            ?: return null

        val userOtpTable = getTable(UserOtp::class.java)
        val queryConditional = sortBeginsWith(
            Key.builder()
                .partitionValue(userId)
                .sortValue(UserTableKeyPrefix.OTP.prefix)
                .build()
        )

        return userOtpTable
            .query(queryConditional)
            .items().firstOrNull {
                it.expiryDate > Instant.now().toString()
            }
    }

    fun findUserPrincipalByUserId(userId: String): UserPrincipal? {
        val userTable = getTable(UserPrincipal::class.java)
        return userTable.getItem(
            Key.builder()
                .partitionValue(userId)
                .sortValue(userId)
                .build()
        )
    }

    fun deleteByUserId(userId: String) {
        val userPrincipalTable = getTable(UserPrincipal::class.java)
        val userOtpTable = getTable(UserOtp::class.java)
        val userLocationTable = getTable(UserLocation::class.java)
        val userIpTable = getTable(UserIp::class.java)

        userPrincipalTable.deleteItem(
            Key.builder()
                .partitionValue(userId)
                .sortValue(userId)
                .build()
        )

        val userOtpWriteBatch = WriteBatch.builder(UserOtp::class.java)
        userOtpTable.query(
            sortBeginsWith(
                Key.builder()
                    .partitionValue(userId)
                    .sortValue(UserTableKeyPrefix.OTP.prefix)
                    .build()
            )
        )
            .items()
            .forEach { userOtpWriteBatch.addDeleteItem(it) }

        val userLocationWriteBatch = WriteBatch.builder(UserLocation::class.java)
        userLocationTable.query(
            sortBeginsWith(
                Key.builder()
                    .partitionValue(userId)
                    .sortValue(UserTableKeyPrefix.LOCATION.prefix)
                    .build()
            )
        )
            .items()
            .forEach { userLocationWriteBatch.addDeleteItem(it) }

        val userIpWriteBatch = WriteBatch.builder(UserIp::class.java)
        userIpTable.query(
            sortBeginsWith(
                Key.builder()
                    .partitionValue(userId)
                    .sortValue(UserTableKeyPrefix.IP.prefix)
                    .build()
            )
        )
            .items()
            .forEach { userIpWriteBatch.addDeleteItem(it) }

        val batchWriteResult = dynamoDbEnhancedClient.batchWriteItem {
            it.writeBatches(
                userOtpWriteBatch.build(),
                userLocationWriteBatch.build(),
                userIpWriteBatch.build()
            )
        }

        for (key in batchWriteResult.unprocessedDeleteItemsForTable(userOtpTable)) {
            println(key)
        }

        for (key in batchWriteResult.unprocessedDeleteItemsForTable(userLocationTable)) {
            println(key)
        }

        for (key in batchWriteResult.unprocessedDeleteItemsForTable(userIpTable)) {
            println(key)
        }
    }

    private fun <T : User> getTable(clazz: Class<T>): DynamoDbTable<T> {
        return dynamoDbEnhancedClient.table("Users", TableSchema.fromBean(clazz))
    }
}
