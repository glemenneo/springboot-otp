package com.example.ipwhitelist.model.dynamodb

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey

data class Applications(
    @DynamoDBHashKey(attributeName = "appId")
    val appId: String,

    @DynamoDBRangeKey(attributeName = "objectId")
    val objectId: String,

    @DynamoDBAttribute(attributeName = "name")
    val name: String,

    @DynamoDBAttribute(attributeName = "description")
    val description: String,

    @DynamoDBAttribute(attributeName = "roleName")
    val roleName: String,

    @DynamoDBAttribute(attributeName = "accountId")
    val accountId: String,

    @DynamoDBAttribute(attributeName = "ipSetId")
    val ipSetId: String,

    @DynamoDBAttribute(attributeName = "role")
    val role: String?,

    @DynamoDBAttribute(attributeName = "TTL")
    val TTL: Long?
)