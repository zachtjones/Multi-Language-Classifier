package com.zachjones.languageclassifier.entities

import com.zachjones.languageclassifier.model.types.Language
import java.util.EnumMap

/***
 * Represents a language decision where there's multiple languages or multiple weighted
 * parts in each decision.
 */
class MultiLanguageDecision : LanguageDecision() {
    private val chances = EnumMap<Language, Double>(Language::class.java)


    /** Adds weight to the specified language.  */
    private fun addWeightTo(language: Language, weight: Double) {
        // insert into the map if not already there
        if (!chances.containsKey(language)) chances[language] = 0.0

        // add on the weight
        chances[language] = chances[language]!! + weight
    }

    /**
     * Adds the weight to the overall language decision, with this decision playing
     * just a fraction of the overall decision.
     * @param decide The language decision for the part.
     * @param weight The relative weight of this part's decision.
     */
    @JvmOverloads
    fun addWeightTo(decide: LanguageDecision, weight: Double = 1.0) {
        for (language in Language.values()) {
            addWeightTo(language, decide.confidenceForLanguage(language) * weight)
        }
    }

    /**
     * Normalizes the weights so that the sum is 1.0
     */
    fun normalize() {
        val totalWeight = chances.values.stream().mapToDouble { i: Double? -> i!! }.sum()
        for (key in chances.keys) {
            chances[key] = chances[key]!! / totalWeight
        }
    }

    override fun confidenceForLanguage(language: Language): Double = chances[language] ?: 0.0

    override fun mostConfidentLanguage(): Language {
        return Language.values().maxBy { this.confidenceForLanguage(it) }
    }
}
