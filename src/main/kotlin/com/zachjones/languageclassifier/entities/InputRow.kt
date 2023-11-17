package com.zachjones.languageclassifier.entities

import examples.GetWikipediaContent
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.io.Serializable
import java.util.Locale

/**
 * Represents a row of input to either the training or testing.
 * For simplicity this will arbitrarily label unlabeled data to English.
 */
class InputRow(content: String) : Serializable {
    val words: List<String>

    /** Null if unlabeled data  */
    var outputValue: String? = null

    /** Parses the input row from text, whether labeled or unlabeled.
     * Precondition: text doesn't have the pipe character in it unless to separate label from content.  */
    init {
        val rawWords: String
        if (content.contains("|")) {
            rawWords = content.substringAfter('|')
            outputValue = content.substringBefore('|')
        } else {
            // unlabeled
            rawWords = content
            outputValue = null
        }
        words = rawWords
            .replace(GetWikipediaContent.REGEX.toRegex(), replacement = "")
            .lowercase()
            .split(' ')
    }

    companion object {
        /** Loads the examples from the file.  */
        @Throws(IOException::class)
        fun loadExamples(exampleFile: String): ArrayList<InputRow> {
            val br = BufferedReader(FileReader(exampleFile))
            var line: String?
            val allData = ArrayList<InputRow>()
            while (br.readLine().also { line = it } != null) {
                allData.add(InputRow(line!!))
            }
            return allData
        }
    }
}
