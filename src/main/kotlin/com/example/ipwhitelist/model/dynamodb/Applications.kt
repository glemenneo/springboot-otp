package com.example.ipwhitelist.model.dynamodb

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

@DynamoDbBean
data class Applications(
    // @see https://github.com/aws/aws-sdk-java-v2/issues/2096 for method annotation
    @get:DynamoDbPartitionKey
    val appId: String,

    @get:DynamoDbSortKey
    @get:DynamoDbSecondaryPartitionKey(indexNames = ["UserGSI"])
    val objectId: String,

    val name: String,

    val description: String,

    val roleName: String,

    val accountId: String,

    val ipSetId: String,

    val role: String?,

    //TODO: enable ttl
    val ttl: Long?
)