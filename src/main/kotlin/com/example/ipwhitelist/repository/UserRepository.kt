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
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException
import java.time.Instant

@Repository
class UserRepository(
    val dynamoDbEnhancedClient: DynamoDbEnhancedClient
) {
    fun <T : User> save(user: T): T? {
        getTable(user.javaClass).putItem(user)
        return user
    }

    fun findUserPrincipalByEmail(email: String): UserPrincipal? {
        val emailGSI = getTable(UserPrincipal::class.java).index("EmailGSI")
        val queryConditional = QueryConditional.keyEqualTo {
            it.partitionValue(email)
        }
        val usersWithEmail: SdkIterable<Page<UserPrincipal>>? = emailGSI.query(
            QueryEnhancedRequest.builder().queryConditional(queryConditional).build()
        )
        return usersWithEmail?.firstOrNull()?.items()?.firstOrNull()
    }

    fun findUserOtpByEmail(email: String): UserOtp? {
        val userKey = this.findUserPrincipalByEmail(email)?.userId ?: return null

        val userOtpTable = getTable(UserOtp::class.java)
        val queryConditional = sortBeginsWith(
            Key.builder().partitionValue(userKey).sortValue(UserTableKeyPrefix.OTP.prefix).build()
        )

        return userOtpTable.query(queryConditional).items().firstOrNull {
            it.expiryDate > Instant.now().toString()
        }
    }

    fun findUserPrincipalByUserId(userKey: String): UserPrincipal? {
        val userTable = getTable(UserPrincipal::class.java)
        return userTable.getItem(
            Key.builder().partitionValue(userKey).sortValue(userKey).build()
        )
    }

    fun deleteByUserId(userKey: String): Boolean {
        val userPrincipalTable = getTable(UserPrincipal::class.java)
        val userOtpTable = getTable(UserOtp::class.java)
        val userLocationTable = getTable(UserLocation::class.java)
        val userIpTable = getTable(UserIp::class.java)

        val userOtpReadBatch = ReadBatch.builder(UserOtp::class.java).mappedTableResource(userOtpTable).addGetItem(
            Key.builder().partitionValue(userKey).sortValue(UserTableKeyPrefix.OTP.prefix).build()
        ).build()
        val userLocationReadBatch =
            ReadBatch.builder(UserLocation::class.java).mappedTableResource(userLocationTable).addGetItem(
                Key.builder().partitionValue(userKey).sortValue(UserTableKeyPrefix.LOCATION.prefix).build()
            ).build()
        val userIpsReadBatch = ReadBatch.builder(UserIp::class.java).mappedTableResource(userIpTable).addGetItem(
            Key.builder().partitionValue(userKey).sortValue(UserTableKeyPrefix.IP.prefix).build()
        ).build()

        val resultPages = dynamoDbEnhancedClient.batchGetItem {
            it.readBatches(
                userOtpReadBatch, userLocationReadBatch, userIpsReadBatch
            )
        }

        try {
            dynamoDbEnhancedClient.transactWriteItems {
                it.addDeleteItem(
                    userPrincipalTable, Key.builder().partitionValue(userKey).sortValue(userKey).build()
                )
                resultPages.resultsForTable(userOtpTable).forEach { userOtp ->
                    it.addDeleteItem(userOtpTable, userOtp)
                }
                resultPages.resultsForTable(userOtpTable).forEach { userOtp ->
                    it.addDeleteItem(userOtpTable, userOtp)
                }
                resultPages.resultsForTable(userLocationTable).forEach { userLocation ->
                    it.addDeleteItem(userLocationTable, userLocation)
                }
                resultPages.resultsForTable(userIpTable).forEach { userIp ->
                    it.addDeleteItem(userIpTable, userIp)
                }
            }
            return true
        } catch (ex: TransactionCanceledException) {
            ex.cancellationReasons().stream().forEach {
                println(it.toString())
            }
            return false
        }
    }

    private fun <T : User> getTable(clazz: Class<T>): DynamoDbTable<T> {
        return dynamoDbEnhancedClient.table("Users", TableSchema.fromBean(clazz))
    }
}
