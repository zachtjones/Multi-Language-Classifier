package com.zachjones.languageclassifier.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
class FileOperationService {

    val mapper = jacksonObjectMapper()

    final inline fun <reified T> readFile(path: Path): T {
        return mapper.readValue(path.toFile())
    }

    fun writeFile(path: Path, content: Any) {
        mapper.writeValue(path.toFile(), content)
    }
}