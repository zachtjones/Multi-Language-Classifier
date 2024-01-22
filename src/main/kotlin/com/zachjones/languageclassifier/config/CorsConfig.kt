package com.zachjones.languageclassifier.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig(private val properties: CorsConfigurationProperties) : WebMvcConfigurer {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun addCorsMappings(registry: CorsRegistry) {
        logger.info("Allowed Domains=${properties.allowedOrigins}")
        registry.addMapping("/**")
            .allowedOrigins(
                *properties.allowedOrigins.toTypedArray()
            )
            .allowCredentials(true)
    }
}

@Configuration
@ConfigurationProperties(prefix = "")
class CorsConfigurationProperties {
    var allowedOrigins: List<String> = emptyList()
}