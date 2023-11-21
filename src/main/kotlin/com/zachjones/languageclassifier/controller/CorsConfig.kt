package com.zachjones.languageclassifier.controller

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "https://www.zach-jones.com",
                "https://zach-jones.com",
                "http://localhost:3000"
            )
            .allowCredentials(true)
    }
}