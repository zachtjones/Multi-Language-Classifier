package com.zachjones.languageclassifier.core

import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.entities.LanguageDecision
import com.zachjones.languageclassifier.model.types.Language

/***
 * Represents a decider with a percentage of confidence known that is not 0 or 1.
 * This is used to distinguish a pair only.
 */
class ConfidenceDecider(
    private val languageOne: Language,
    private val languageTwo: Language,
    private val fractionOne: Double
) : LanguageDecision(), Decider {
    /**
     * Creates a LanguageDecision based on the confidence between the two langauges.
     * @param languageOne The first language
     * @param languageTwo The second language
     * @param fractionOne The fraction that is the first language (in range [0.0, 1.0])
     */
    init {
        require(!fractionOne.isNaN()) { "Confidence decider can not be NaN" }
    }

    override fun decide(row: InputRow): LanguageDecision {
        return this
    }

    override fun representation(numSpaces: Int): String {
        return "return " + languageOne + " with " + fractionOne + ", languageTwo with " + (1 - fractionOne)
    }

    override fun confidences(): Map<Language, Double> {
        return mapOf(
            languageOne to fractionOne,
            languageTwo to 1.0 - fractionOne
        )
    }
}
