package com.example.ipwhitelist.model.dynamodb


import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

@DynamoDbBean()
data class User(
    // @see https://github.com/aws/aws-sdk-java-v2/issues/2096 for method annotation
    @get:DynamoDbPartitionKey
    val userId: String,

    @get:DynamoDbSortKey
    val objectId: String,

    @get:DynamoDbSecondaryPartitionKey(indexNames = ["EmailGSI"])
    val email: String,

    val role: String,

    val ip: String?,

    var otp: String?,

    val expiryDate: String?,

    //TODO: enable ttl
    val ttl: Long?,
)
