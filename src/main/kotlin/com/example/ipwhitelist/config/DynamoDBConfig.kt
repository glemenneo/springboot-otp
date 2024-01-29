package com.example.ipwhitelist.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.model.ListTablesRequest
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@EnableDynamoDBRepositories(basePackages = ["com.example.ipwhitelist.repository"])
class DynamoDBConfig(
    @Value("\${amazon.aws.accessKey}") private val accessKey: String,
    @Value("\${amazon.aws.secretKey}") private val secretKey: String,
    @Value("\${amazon.aws.region}") private val region: String
) {

    @Primary
    @Bean
    fun dynamoDBMapper(amazonDynamoDB: AmazonDynamoDB): DynamoDBMapper {
        return DynamoDBMapper(amazonDynamoDB, DynamoDBMapperConfig.DEFAULT)
    }

    @Bean
    fun amazonDynamoDB(): AmazonDynamoDB {
        val awsCredentials = BasicAWSCredentials(accessKey, secretKey)
        val awsCredentialsProvider = AWSStaticCredentialsProvider(awsCredentials)

        val dynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withRegion(region)
            .build()

        try {
            val tableNames = dynamoDB.listTables(ListTablesRequest()).tableNames
            println("Connected to DynamoDB. Tables: $tableNames")
        } catch (e: Exception) {
            println("Failed to connect to DynamoDB. Error: ${e.message}")
        }

        return dynamoDB
    }

    @Bean
    fun awsCredentials() = BasicAWSCredentials(accessKey, secretKey)
}