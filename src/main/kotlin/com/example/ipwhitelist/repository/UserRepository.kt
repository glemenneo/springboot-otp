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
import java.time.Instant
import java.util.Date

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
        val userTable = getTable(User::class.java)
        userTable.deleteItem(
            Key.builder()
                .partitionValue(userId)
                .build()
        )
    }

    private fun <T : User> getTable(clazz: Class<T>): DynamoDbTable<T> {
        return dynamoDbEnhancedClient.table("Users", TableSchema.fromBean(clazz))
    }
}
