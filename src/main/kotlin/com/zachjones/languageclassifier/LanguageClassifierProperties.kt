package com.zachjones.languageclassifier

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "")
class LanguageClassifierProperties {
    var environment: String = ""
}