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
open class User(
    @get:DynamoDbPartitionKey
    open var userId: String,

    @get:DynamoDbSortKey
    open var objectId: String
)

@DynamoDbBean
data class UserPrincipal(
    @get:DynamoDbPartitionKey
    override var userId: String,

    override var objectId: String,

    @get:DynamoDbSortKey
    var userSortKey: String = DataClassMappings.USER_PRINCIPAL_PREFIX + objectId,

    @get:DynamoDbSecondaryPartitionKey(indexNames = ["EmailGSI"])
    var email: String,

    var role: String
) : User(userId, objectId) {
    constructor() : this("", "", "", "", "")
}

@DynamoDbBean
data class UserLocation(
    @get:DynamoDbPartitionKey
    override var userId: String,

    override var objectId: String,

    @get:DynamoDbSortKey
    var locationSort: String = DataClassMappings.USER_LOCATION_PREFIX + objectId,

    var location: String?
) : User(userId, objectId) {
    constructor() : this("", "", "", "")
}

@DynamoDbBean
data class UserIp(
    @get:DynamoDbPartitionKey
    override var userId: String,

    override var objectId: String,

    @get:DynamoDbSortKey
    var ipSortKey: String = DataClassMappings.USER_IP_PREFIX + objectId,

    var ip: String?,

    var ttl: Long?
) : User(userId, objectId) {
    constructor() : this("", "", "", "", null)
}

@DynamoDbBean
data class UserOtp(
    @get:DynamoDbPartitionKey
    override var userId: String,

    override var objectId: String,

    @get:DynamoDbSortKey
    var otpSortKey: String = DataClassMappings.USER_OTP_PREFIX + objectId,

    var otp: String?,

    var expiryDate: String?,

    var ttl: Long?
) : User(userId, objectId) {
    constructor() : this("", "", "", "", "", null)
}
