package com.example.ipwhitelist.repository

import com.example.ipwhitelist.model.dynamodb.User
import com.example.ipwhitelist.model.dynamodb.UserOtp
import com.example.ipwhitelist.model.dynamodb.UserPrincipal
import org.springframework.stereotype.Repository
import software.amazon.awssdk.core.pagination.sync.SdkIterable
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.Page
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

@Repository
class UserRepository(
    val dynamoDbEnhancedClient: DynamoDbEnhancedClient
) {
    fun <T : User> save(user: T): T {
        val userTable = getTable(user.javaClass)
        userTable.putItem(user)
        return user
    }

    fun findByEmail(email: String): UserPrincipal? {
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
        val userTable = getTable(UserOtp::class.java)
        val emailGSI = userTable.index("EmailGSI")
        val queryConditional = QueryConditional.keyEqualTo {
            it.partitionValue(email)
        }
        val usersWithEmail: SdkIterable<Page<UserOtp>>? = emailGSI.query(
            QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build()
        )
        return usersWithEmail?.firstOrNull()?.items()?.firstOrNull()
    }

    fun findUserByUserId(userId: String): User? {
        val userTable = getTable(User::class.java)
        return userTable.getItem(
            Key.builder()
                .partitionValue(userId)
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
