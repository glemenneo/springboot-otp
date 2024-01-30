package com.example.ipwhitelist.model.dynamodb

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

object DataClassMappings {
    const val USER_PRINCIPAL_PREFIX = "user@"
    const val USER_LOCATION_PREFIX = "location@"
    const val USER_IP_PREFIX = "ip@"
    const val USER_OTP_PREFIX = "otp@"
}

@DynamoDbBean
abstract class User(
    @get:DynamoDbPartitionKey
    open var userId: String,

    @get:DynamoDbSortKey
    open var objectId: String
) {
    constructor() : this("", "")
}


@DynamoDbBean
data class UserPrincipal(
    @get:DynamoDbPartitionKey
    override var userId: String,

    @get:DynamoDbSortKey
    override var objectId: String,

    @get:DynamoDbSecondaryPartitionKey(indexNames = ["EmailGSI"])
    var email: String,

    var role: String
) : User(userId, DataClassMappings.USER_PRINCIPAL_PREFIX + objectId) {
    constructor() : this("", "", "", "")
}

@DynamoDbBean
data class UserLocation(
    @get:DynamoDbPartitionKey
    override var userId: String,

    @get:DynamoDbSortKey
    override var objectId: String,

    var location: String?
) : User(userId, DataClassMappings.USER_LOCATION_PREFIX + objectId) {
    constructor() : this("", "", "")
}

@DynamoDbBean
data class UserIp(
    @get:DynamoDbPartitionKey
    override var userId: String,

    @get:DynamoDbSortKey
    override var objectId: String,

    var ip: String?,

    var ttl: Long?
) : User(userId, DataClassMappings.USER_IP_PREFIX + objectId) {
    constructor() : this("", "", "", null)
}

@DynamoDbBean
data class UserOtp(
    @get:DynamoDbPartitionKey
    override var userId: String,

    @get:DynamoDbSortKey
    override var objectId: String,

    var otp: String?,

    var expiryDate: String?,

    var ttl: Long?
) : User(userId, DataClassMappings.USER_OTP_PREFIX + objectId) {
    constructor() : this("", "", "", "", null)
}

