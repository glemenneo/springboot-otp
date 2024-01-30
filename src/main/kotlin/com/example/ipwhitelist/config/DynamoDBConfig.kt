package com.example.ipwhitelist.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

@Configuration
class DynamoDBConfig(
    @Value("\${amazon.aws.accessKey}") private val accessKey: String,
    @Value("\${amazon.aws.secretKey}") private val secretKey: String,
    @Value("\${amazon.aws.region}") private val region: String
) {
    @Bean
    fun getDynamoDbClient(): DynamoDbEnhancedClient {
        val credentialsProvider = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey)
        )

//        val credentialsProvider = DefaultCredentialsProvider.builder()
//            .profileName(profile)
//            .build()

        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(
                DynamoDbClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider)
                    .build()
            )
            .build()
    }
}