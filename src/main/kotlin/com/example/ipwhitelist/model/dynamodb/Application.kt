package com.example.ipwhitelist.model.dynamodb

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

enum class AppTableKeyPrefix(val prefix: String) {
    APP("APP-"),
    USER("USER-"),
}
@DynamoDbBean
open class Application(
    @get:DynamoDbPartitionKey
    open var appId: String,

    @get:DynamoDbSortKey
    open var objectId: String
)

@DynamoDbBean
data class ApplicationDetails(
    @get:DynamoDbPartitionKey
    override var appId: String,

    @get:DynamoDbSortKey
    override var objectId: String,

    var name : String,
    var description : String,
    var accountId : String,
    var changeToken: String,
    var ipSetId: String,

) : Application(appId, AppTableKeyPrefix.APP.prefix + objectId) {
    constructor() : this("", "", "", "", "", "", "")
}

@DynamoDbBean
data class ApplicationUser(
    @get:DynamoDbPartitionKey
    override var appId: String,

    @get:DynamoDbSortKey
    override var objectId: String,

    var role: String,

) : Application(appId, AppTableKeyPrefix.USER.prefix + objectId) {
    constructor() : this("", "", "")
}