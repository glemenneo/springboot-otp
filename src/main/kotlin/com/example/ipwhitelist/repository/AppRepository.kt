package com.example.ipwhitelist.repository

import com.example.ipwhitelist.model.EnhancedAppResponse
import com.example.ipwhitelist.model.dynamodb.*
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.*
import software.amazon.awssdk.enhanced.dynamodb.model.*
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.sortBeginsWith
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.util.*
import kotlin.collections.ArrayList

@Repository
class AppRepository(
    val dynamoDbEnhancedClient: DynamoDbEnhancedClient
) {
    fun <T : Application> save(application: T): T {
        val applicationTable = getTable(application.javaClass)
        applicationTable.putItem(application)
        return application
    }

    fun getAppsByUserId(userId: String) : List<EnhancedAppResponse> {

        val appUsersTable = getTable(ApplicationUser::class.java)
        val appGSI = appUsersTable.index("UserGSI")

        val appsQueryConditional = QueryConditional.keyEqualTo {
            it.partitionValue(userId)
        }

        val appsQueryEnhancedRequest = QueryEnhancedRequest.builder()
            .queryConditional(appsQueryConditional)
            .build()

        val authorizedAppsIterator = appGSI.query(appsQueryEnhancedRequest)
        val authorizedApps: MutableList<ApplicationUser> = ArrayList()

        for (page: Page<ApplicationUser> in authorizedAppsIterator) {
            authorizedApps.addAll(page.items())
        }

        // using the authorized appIds, fetch the corresponding app details from the ApplicationDetails table
        val appDetailsTable = getTable(ApplicationDetails::class.java)
        val uniqueAppIds = authorizedApps.map { it.appId }.toSet()

        val appDetailsReadBatch = ReadBatch.builder(ApplicationDetails::class.java)
            .mappedTableResource(appDetailsTable)

        uniqueAppIds.forEach { appId ->
            val queryConditional = sortBeginsWith(
                Key.builder()
                    .partitionValue(appId)
                    .sortValue(AppTableKeyPrefix.APP.prefix)
                    .build()
            )

            val queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build()

            val appDetailsIterator = appDetailsTable.query(queryEnhancedRequest)

            appDetailsIterator.items()
                .forEach { appDetailsReadBatch.addGetItem(it) }
        }

        val appDetailsBatchResults = dynamoDbEnhancedClient.batchGetItem(
            BatchGetItemEnhancedRequest.builder()
                .readBatches(appDetailsReadBatch.build())
                .build()
        ).resultsForTable(appDetailsTable)

        val enhancedAppResponses: MutableList<EnhancedAppResponse> = ArrayList()

        appDetailsBatchResults.forEach { appDetails ->
            val appUser = authorizedApps.firstOrNull { it.appId == appDetails.appId }
            appUser?.let {
                enhancedAppResponses.add(
                    EnhancedAppResponse(
                        appId = appDetails.appId,
                        name = appDetails.name,
                        description = appDetails.description,
                        role = appUser.role
                    )
                )
            }
        }

        return enhancedAppResponses
    }

    fun findAppDetailsByAppId(appId: String) : ApplicationDetails? {
        val appDetailsTable = getTable(ApplicationDetails::class.java)
        val queryConditional = sortBeginsWith(
            Key.builder()
                .partitionValue(AppTableKeyPrefix.APP.prefix + appId)
                .sortValue(AppTableKeyPrefix.APP.prefix)
                .build()
        )

        return appDetailsTable
            .query(queryConditional)
            .items().firstOrNull()
    }

    fun findUsersByAppId(appId: String) : List<String> {
        val appUsersTable = getTable(ApplicationUser::class.java)
        val queryConditional = sortBeginsWith(
            Key.builder()
                .partitionValue(AppTableKeyPrefix.APP.prefix + appId)
                .sortValue(AppTableKeyPrefix.USER.prefix)
                .build()
        )

        val queryEnhancedRequest = QueryEnhancedRequest.builder()
            .queryConditional(queryConditional)
            .filterExpression(
                Expression.builder()
                    .expression("#role = :role")
                    .putExpressionValue(":role", AttributeValue.builder().s("USER").build())
                    .expressionNames(Collections.singletonMap("#role", "role"))
                    .build()
            )
            .build()

        val appAdminsIterator = appUsersTable.query(queryEnhancedRequest)

        val appUsers: MutableList<String> = ArrayList()
        appAdminsIterator.items().forEach { appUsers.add(it.objectId.substringAfter(AppTableKeyPrefix.USER.prefix)) }

        return appUsers
    }

    fun findAdminsByAppId(appId: String) : List<String> {
        val appUsersTable = getTable(ApplicationUser::class.java)
        val queryConditional = sortBeginsWith(
            Key.builder()
                .partitionValue(AppTableKeyPrefix.APP.prefix + appId)
                .sortValue(AppTableKeyPrefix.USER.prefix)
                .build()
        )

        val queryEnhancedRequest = QueryEnhancedRequest.builder()
            .queryConditional(queryConditional)
            .filterExpression(
                Expression.builder()
                    .expression("#role = :role")
                    .putExpressionValue(":role", AttributeValue.builder().s("ADMIN").build())
                    .expressionNames(Collections.singletonMap("#role", "role"))
                    .build()
            )
            .build()

        val appAdminsIterator = appUsersTable.query(queryEnhancedRequest)

        val appAdmins: MutableList<String> = ArrayList()
        appAdminsIterator.items().forEach { appAdmins.add(it.objectId.substringAfter(AppTableKeyPrefix.USER.prefix)) }

        return appAdmins
    }

    fun deleteApp(appId: String) {
        val appDetailsTable = getTable(ApplicationDetails::class.java)
        val appUsersTable = getTable(ApplicationUser::class.java)

        val appDetailsWriteBatch = WriteBatch.builder(ApplicationDetails::class.java)
            .mappedTableResource(appDetailsTable)

        appDetailsTable.query(
            sortBeginsWith(
                Key.builder()
                    .partitionValue(appId)
                    .sortValue(AppTableKeyPrefix.APP.prefix)
                    .build()
            )
        ).items()
            .forEach { appDetailsWriteBatch.addDeleteItem(it) }

        val appUsersWriteBatch = WriteBatch.builder(ApplicationUser::class.java)
            .mappedTableResource(appUsersTable)

        appUsersTable.query(
            sortBeginsWith(
                Key.builder()
                    .partitionValue(appId)
                    .sortValue(AppTableKeyPrefix.USER.prefix)
                    .build()
            )
        ).items()
            .forEach { appUsersWriteBatch.addDeleteItem(it) }

        val batchWriteRequest = BatchWriteItemEnhancedRequest.builder()
            .writeBatches(
                appDetailsWriteBatch.build(),
                appUsersWriteBatch.build()
            )
            .build()

        val batchWriteResult = dynamoDbEnhancedClient.batchWriteItem(batchWriteRequest)

        for (key in batchWriteResult.unprocessedDeleteItemsForTable(appDetailsTable)) {
            println(key)
        }

        for (key in batchWriteResult.unprocessedDeleteItemsForTable(appUsersTable)) {
            println(key)
        }
    }


    fun deleteUser(appId: String, userId: String) {
        val appUsersTable = getTable(ApplicationUser::class.java)

        val userToDelete = appUsersTable.query(
            sortBeginsWith(
                Key.builder()
                    .partitionValue(AppTableKeyPrefix.APP.prefix + appId)
                    .sortValue(AppTableKeyPrefix.USER.prefix + userId)
                    .build()
            )
        ).items().firstOrNull()

        userToDelete?.let { appUsersTable.deleteItem(it) }
    }

    private fun <T : Application> getTable(clazz: Class<T>): DynamoDbTable<T> {
        return dynamoDbEnhancedClient.table("Applications", TableSchema.fromBean(clazz))
    }
}