package com.example.ipwhitelist.model.dynamodb

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

object ApplicationClassMappings {
    const val APP_INFO_PREFIX = "app@"
    const val APP_USER_PREFIX = "user@"
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

) : Application(appId, ApplicationClassMappings.APP_INFO_PREFIX + objectId) {
    constructor() : this("", "", "", "", "", "", "")
}

@DynamoDbBean
data class ApplicationUser(
    @get:DynamoDbPartitionKey
    override var appId: String,

    @get:DynamoDbSortKey
    override var objectId: String,

    var role: String,

) : Application(appId, ApplicationClassMappings.APP_USER_PREFIX + objectId) {
    constructor() : this("", "", "")
}