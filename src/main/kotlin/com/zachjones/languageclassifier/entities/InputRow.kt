package com.zachjones.languageclassifier.entities

import com.zachjones.languageclassifier.model.types.Language
import java.io.Serializable

/**
 * Represents a row of input to either the training or testing.
 * Testing input will typically have the language be null
 */
data class InputRow(val language: Language?, val words: List<String>): Serializable {

    constructor(language: Language?, words: String) : this(
        language = language,
        words = parseWords(words)
    )

    companion object {
        private fun parseWords(content: String) = content
            .replace(WORD_SPLIT_REGEX, "")
            .split(' ')
            .filter { it.isNotBlank() }
    }
}
