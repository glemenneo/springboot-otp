package com.example.ipwhitelist.model.dynamodb


import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

@DynamoDbBean()
data class User(
    // @see https://github.com/aws/aws-sdk-java-v2/issues/2096 for method annotation
    @get:DynamoDbPartitionKey
    var userId: String,

    @get:DynamoDbSortKey
    var objectId: String,

    @get:DynamoDbSecondaryPartitionKey(indexNames = ["EmailGSI"])
    var email: String,

    var role: String,

    var ip: String?,

    var otp: String?,

    var expiryDate: String?,

    //TODO: enable ttl
    var ttl: Long?,

) {
//    constructor() : this("", "", "", "", null, null, null, null)
}
