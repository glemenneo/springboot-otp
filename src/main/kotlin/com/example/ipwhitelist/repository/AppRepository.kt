package com.example.ipwhitelist.repository

import com.example.ipwhitelist.model.EnhancedAppResponse
import com.example.ipwhitelist.model.dynamodb.Application
import com.example.ipwhitelist.model.dynamodb.AppTableKeyPrefix
import com.example.ipwhitelist.model.dynamodb.ApplicationDetails
import com.example.ipwhitelist.model.dynamodb.ApplicationUser
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

    fun getAllApps(): List<ApplicationDetails> {
        val appTable = getTable(ApplicationDetails::class.java)
        val items: MutableList<ApplicationDetails> = ArrayList()

        val scanEnhancedRequest = ScanEnhancedRequest.builder()
            .filterExpression(
                Expression.builder()
                    .expression("begins_with(objectId, :prefix)")
                    .expressionValues(Collections.singletonMap(
                        ":prefix", AttributeValue.builder()
                            .s(AppTableKeyPrefix.APP.prefix).build()))
                    .build()
            )
            .build()

        val pageIterable: PageIterable<ApplicationDetails> = appTable.scan(scanEnhancedRequest)

        for (page: Page<ApplicationDetails> in pageIterable) {
            items.addAll(page.items())
        }

        return items
    }

    fun getAppsByUserId(userId: String) : List<EnhancedAppResponse> {

        val scanEnhancedRequest = ScanEnhancedRequest.builder()
            .filterExpression(
                Expression.builder()
                    .expression("objectId = :objId")
                    .expressionValues(
                        Collections.singletonMap(
                            ":objId", AttributeValue.builder()
                                .s(userId).build()
                        )
                    )
                    .build()
            )
            .build()

        val appUsersTable = getTable(ApplicationUser::class.java)
        val pageIterable: PageIterable<ApplicationUser> = appUsersTable.scan(scanEnhancedRequest)

        val authorizedApps: MutableList<ApplicationUser> = ArrayList()
        for (page: Page<ApplicationUser> in pageIterable) {
            authorizedApps.addAll(page.items())
        }

        // using the authorized appIds, fetch the corresponding app details from the ApplicationDetails table
        val appDetailsTable = getTable(ApplicationDetails::class.java)
        val enhancedAppResponses: MutableList<EnhancedAppResponse> = ArrayList()

        authorizedApps.forEach { app ->
            val queryConditional = sortBeginsWith(
                Key.builder()
                    .partitionValue(app.appId)
                    .sortValue(AppTableKeyPrefix.APP.prefix)
                    .build()
            )

            val queryEnhancedRequest = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .build()

            val appDetailsIterator = appDetailsTable.query(queryEnhancedRequest)

            appDetailsIterator.items().forEach { appDetails ->
                val appUser = authorizedApps.firstOrNull { app -> app.appId == appDetails.appId }
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

        val appAdmins: MutableList<String> = ArrayList()
        appAdminsIterator.items().forEach { appAdmins.add(it.objectId.substringAfter(AppTableKeyPrefix.USER.prefix)) }

        return appAdmins
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

    fun deleteApp(appIp : String) {
        val appDetailsTable = getTable(ApplicationDetails::class.java)
        val appUsersTable = getTable(ApplicationUser::class.java)

        appDetailsTable.query(
            sortBeginsWith(
                Key.builder()
                    .partitionValue(appIp)
                    .sortValue(AppTableKeyPrefix.APP.prefix)
                    .build()
            )
        ).items()
            .forEach(appDetailsTable::deleteItem)

        appUsersTable.query(
            sortBeginsWith(
                Key.builder()
                    .partitionValue(appIp)
                    .sortValue(AppTableKeyPrefix.USER.prefix)
                    .build()
            )
        ).items()
            .forEach(appUsersTable::deleteItem)
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