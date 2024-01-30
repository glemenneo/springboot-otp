package com.example.ipwhitelist.repository

import com.example.ipwhitelist.model.dynamodb.Application
import com.example.ipwhitelist.model.dynamodb.ApplicationClassMappings
import com.example.ipwhitelist.model.dynamodb.ApplicationDetails
import com.example.ipwhitelist.model.dynamodb.ApplicationUser
import org.springframework.stereotype.Repository
import software.amazon.awssdk.enhanced.dynamodb.*
import software.amazon.awssdk.enhanced.dynamodb.model.Page
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional.sortBeginsWith
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest
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
                            .s(ApplicationClassMappings.APP_INFO_PREFIX).build()))
                    .build()
            )
            .build()

        val pageIterable: PageIterable<ApplicationDetails> = appTable.scan(scanEnhancedRequest)

        for (page: Page<ApplicationDetails> in pageIterable) {
            items.addAll(page.items())
        }

        return items
    }

    fun findAppDetailsByAppId(appId: String) : ApplicationDetails? {
        val appDetailsTable = getTable(ApplicationDetails::class.java)
        val queryConditional = sortBeginsWith(
            Key.builder()
                .partitionValue(appId)
                .sortValue(ApplicationClassMappings.APP_INFO_PREFIX)
                .build()
        )

        return appDetailsTable
            .query(queryConditional)
            .items().firstOrNull()
    }

    fun deleteApp(appIp : String) {
        val appDetailsTable = getTable(ApplicationDetails::class.java)
        val appUsersTable = getTable(ApplicationUser::class.java)

        appDetailsTable.query(
            sortBeginsWith(
                Key.builder()
                    .partitionValue(appIp)
                    .sortValue(ApplicationClassMappings.APP_INFO_PREFIX)
                    .build()
            )
        ).items()
            .forEach(appDetailsTable::deleteItem)

        appUsersTable.query(
            sortBeginsWith(
                Key.builder()
                    .partitionValue(appIp)
                    .sortValue(ApplicationClassMappings.APP_USER_PREFIX)
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
                    .partitionValue(appId)
                    .sortValue(ApplicationClassMappings.APP_USER_PREFIX + userId)
                    .build()
            )
        ).items().firstOrNull()

        userToDelete?.let { appUsersTable.deleteItem(it) }
    }

    private fun <T : Application> getTable(clazz: Class<T>): DynamoDbTable<T> {
        return dynamoDbEnhancedClient.table("Applications", TableSchema.fromBean(clazz))
    }
}