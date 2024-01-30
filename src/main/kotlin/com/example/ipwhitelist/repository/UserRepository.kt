package com.example.ipwhitelist.repository

import com.example.ipwhitelist.model.dynamodb.User
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
    fun save(user: User): User {
        val userTable = getTable()
        userTable.putItem(user)
        return user
    }

    fun findByEmail(email: String): User? {
        val userTable = getTable()
        val emailGSI = userTable.index("EmailGSI")
        val queryConditional = QueryConditional.keyEqualTo {
            it.partitionValue(email)
        }
        val usersWithEmail: SdkIterable<Page<User>> = emailGSI.query(
            QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build()
        )
        return usersWithEmail.firstOrNull()?.items()?.firstOrNull()
    }

    fun findUserByUserId(userId: String): User? {
        val userTable = getTable()
        return userTable.getItem(
            Key.builder()
                .partitionValue(userId)
                .build()
        )
    }

    fun deleteByUserId(userId: String) {
        val userTable = getTable()
        userTable.deleteItem(
            Key.builder()
                .partitionValue(userId)
                .build()
        )
    }

    private fun getTable(): DynamoDbTable<User> {
        return dynamoDbEnhancedClient.table("Users", TableSchema.fromBean(User::class.java))
    }
}