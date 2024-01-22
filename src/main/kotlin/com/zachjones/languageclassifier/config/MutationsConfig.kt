package com.zachjones.languageclassifier.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "")
class MutationsConfig {

    private val logger = LoggerFactory.getLogger(this::class.java)

    var disableMutations: Boolean = false
        set(value) {
            logger.info("Mutations disabled=$value")
            field = value
        }
}