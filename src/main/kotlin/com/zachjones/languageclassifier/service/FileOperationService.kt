package com.zachjones.languageclassifier.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import java.nio.file.Path
import kotlin.io.path.readText

@Component
class FileOperationService {

    val mapper = jacksonObjectMapper()

    final inline fun <reified T> readJsonFile(path: Path): T {
        return mapper.readValue(path.toFile())
    }

    fun readTextFile(path: Path): String {
        return path.readText()
    }

    fun writeJsonFile(path: Path, content: Any) {
        mapper.writeValue(path.toFile(), content)
    }
}