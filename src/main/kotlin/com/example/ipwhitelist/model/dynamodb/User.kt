package com.example.ipwhitelist.model.dynamodb

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable
import org.springframework.data.annotation.Id

@DynamoDBTable(tableName = "Users")
data class User(
    @Id
    @DynamoDBHashKey(attributeName = "userId")
    var userId: String,

    @DynamoDBHashKey(attributeName = "objectId")
    var objectId: String,

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "email-index", attributeName = "email")
    var email: String,

    @DynamoDBAttribute(attributeName = "role")
    var role: String,

    @DynamoDBAttribute(attributeName = "ip")
    var ip: String?,

    @DynamoDBAttribute(attributeName = "otp")
    var otp: String?,

    @DynamoDBAttribute(attributeName = "expiryDate")
    var expiryDate: String?,

    @DynamoDBAttribute(attributeName = "TTL")
    var ttl: Long?,
) {
    constructor() : this("", "", "", "", null, null, null, null)
}
