package com.example.ipwhitelist.config

import com.example.ipwhitelist.model.Otp
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class RedisConfig {
    @Bean
    public fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Otp> {
        val template = RedisTemplate<String, Otp>()
        template.connectionFactory = redisConnectionFactory
        return template
    }
}