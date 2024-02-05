package com.example.ipwhitelist.model.dynamodb

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

enum class UserTableKeyPrefix(val prefix: String) {
    USER("USER-"),
    OTP("OTP-"),
    LOCATION("LOCATION-"),
    IP("IP-")
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
) : User(userId = "${UserTableKeyPrefix.USER.prefix}$userId",
    objectId = "${UserTableKeyPrefix.USER.prefix}$objectId"
) {
    constructor() : this("", "", "", "")
}

@DynamoDbBean
data class UserLocation(
    @get:DynamoDbPartitionKey
    override var userId: String,

    @get:DynamoDbSortKey
    override var objectId: String,

    var name: String
) : User(
    userId = "${UserTableKeyPrefix.USER.prefix}$userId",
    objectId = "${UserTableKeyPrefix.LOCATION.prefix}$objectId"
) {
    constructor() : this("", "", "")
}

@DynamoDbBean
data class UserIp(
    @get:DynamoDbPartitionKey
    override var userId: String,

    @get:DynamoDbSortKey
    override var objectId: String,

    var ip: String,

    var ttl: Long
) : User(userId = "${UserTableKeyPrefix.USER.prefix}$userId",
    objectId = "${UserTableKeyPrefix.IP.prefix}$objectId"
) {
    constructor() : this("", "", "", 0)
}

@DynamoDbBean
data class UserOtp(
    @get:DynamoDbPartitionKey
    override var userId: String,

    @get:DynamoDbSortKey
    override var objectId: String,

    var otp: String,

    var expiryDate: String,

    var ttl: Long
) : User(userId = "${UserTableKeyPrefix.USER.prefix}$userId",
    objectId = "${UserTableKeyPrefix.OTP.prefix}$objectId"
) {
    constructor() : this("", "", "", "", 0)
}
