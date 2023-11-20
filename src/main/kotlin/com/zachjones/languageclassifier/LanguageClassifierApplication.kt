package com.zachjones.languageclassifier

import com.zachjones.languageclassifier.entities.DATA_PATH
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.FileOutputStream
import java.nio.file.Path
import kotlin.io.path.createDirectories

@SpringBootApplication
class LanguageClassifierApplication

fun main(args: Array<String>) {
    setup()
    runApplication<LanguageClassifierApplication>(*args)
}

private val logger = LoggerFactory.getLogger(LanguageClassifierApplication::class.java)

private fun setup() {
    // create directory for data and copy existing files from the resources - if it does not exist
    Path.of(DATA_PATH).createDirectories()
    // names
    val names = String(readResourceTextFile("${DATA_PATH}data-list.txt")).lines().filter { it.isNotEmpty() }
    // copy resources
    for (name in names) {
        val path = "${DATA_PATH}$name"
        val content = readResourceTextFile(path)

        FileOutputStream(path).also {
            it.write(content)
        }.close()
    }
}

private fun readResourceTextFile(path: String): ByteArray {
    logger.info("Reading $path from resources")

    return LanguageClassifierApplication::class.java.classLoader.getResource(path)!!.readBytes()
}
