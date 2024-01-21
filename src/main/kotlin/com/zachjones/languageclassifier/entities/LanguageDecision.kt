package com.zachjones.languageclassifier.entities

import com.zachjones.languageclassifier.model.types.Language
import main.Learning
import java.util.stream.Collectors

/***
 * Represents a probability distribution of a language choice.
 * This factors in the confidence of the binary decision on each element
 * to make the multi-classifier more accurate.
 */
abstract class LanguageDecision {
    /** Returns the confidence for a language choice.
     * This should be a number between 0 (definitely not the language) to 1.0 (certainly the language)
     * The sum for all languages in the set defined should be 1.0  */
    abstract fun confidenceForLanguage(language: Language): Double

    /**
     * Returns the most confident language.
     */
    abstract fun mostConfidentLanguage(): Language

    override fun toString(): String {
        val mostCommon = mostConfidentLanguage()
        // sort these by the most probable
        val confidences = Language.values()
                .sortedBy { this.confidenceForLanguage(it) }
                .joinToString(separator = " ") {
                    String.format("%s=%.1f%%", it.name, confidenceForLanguage(it) * 100.0)
                }
        return "Decision: $mostCommon with probabilities: $confidences"
    }
}
