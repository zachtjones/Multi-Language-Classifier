package com.zachjones.languageclassifier.entities

import com.zachjones.languageclassifier.model.types.Language
import java.util.EnumMap

/** Represents a single language decision, where it will answer with 100 confidence for the given language.  */
class SingleLanguageDecision private constructor(
        /** The language to always pick  */
        private val language: Language
) : LanguageDecision() {

    override fun confidenceForLanguage(language: Language): Double = if (this.language == language) 1.0 else 0.0

    override fun mostConfidentLanguage(): Language = this.language

    companion object {
        private val decisions = EnumMap<Language, SingleLanguageDecision>(Language::class.java).also { map ->
            Language.values().forEach {
                map[it] = SingleLanguageDecision(it)
            }
        }

        /** Returns the decider to always decide input is the language specified.
         * Uses a cache to reduce object creations.  */
        fun fromLanguage(language: Language): SingleLanguageDecision = decisions[language]!!
    }
}
