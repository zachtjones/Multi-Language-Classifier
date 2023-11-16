package com.zachjones.languageclassifier

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LanguageClassifierApplication

fun main(args: Array<String>) {
    runApplication<LanguageClassifierApplication>(*args)
}
