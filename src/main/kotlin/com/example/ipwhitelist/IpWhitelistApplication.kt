package com.example.ipwhitelist

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class IpWhitelistApplication

fun main(args: Array<String>) {
	runApplication<IpWhitelistApplication>(*args)
}
