package com.zachjones.languageclassifier.entities

import com.zachjones.languageclassifier.model.types.Language

/***
 * Represents a probability distribution of a language choice.
 * This factors in the confidence of the binary decision on each element
 * to make the multi-classifier more accurate.
 */
abstract class LanguageDecision {

    /**
     * Returns the most confident language.
     */
    fun mostConfidentLanguage(): Language = confidences().maxBy { it.value }.key

    abstract fun confidences(): Map<Language, Double>

    override fun toString(): String {
        val mostCommon = mostConfidentLanguage()
        // sort these by the most probable
        val confidences = confidences()
                .entries.sortedBy { it.value }
                .joinToString(separator = " ") {
                    String.format("%s=%.1f%%", it.key, it.value * 100.0)
                }
        return "Decision: $mostCommon with probabilities: $confidences"
    }
}
