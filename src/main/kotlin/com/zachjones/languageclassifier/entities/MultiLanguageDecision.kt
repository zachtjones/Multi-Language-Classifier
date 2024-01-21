package com.zachjones.languageclassifier.entities

import com.zachjones.languageclassifier.model.types.Language

/***
 * Represents a language decision where there's multiple languages or multiple weighted
 * parts in each decision.
 */
data class MultiLanguageDecision(
    val weights: Map<Language, Double>
) : LanguageDecision() {

    /**
     * Normalizes the weights so that the sum is 1.0
     */
    fun normalized(): MultiLanguageDecision {
        val totalWeight = weights.values.sum()
        return MultiLanguageDecision(weights = weights.mapValues { (_, value) ->
            value / totalWeight
        })
    }

    override fun confidences(): Map<Language, Double> = weights

    companion object {
        fun of(decisions: List<LanguageDecision>): MultiLanguageDecision {
            val map = mutableMapOf<Language, Double>()
            Language.values().forEach {
                map[it] = 0.0
            }
            decisions.forEach { decision ->
                decision.confidences().map { (language, confidence) ->
                    map[language] = map[language]!! + confidence
                }
            }
            // remove all the "votes" for other, and let the majority language win
            map[Language.OTHER] = 0.0
            return MultiLanguageDecision(map).normalized()
        }
    }
}

