package com.zachjones.languageclassifier.core

import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.entities.LanguageDecision
import com.zachjones.languageclassifier.entities.SingleLanguageDecision
import com.zachjones.languageclassifier.model.types.Language
import java.util.EnumMap

/** Represents an absolute decision on a language.
 * This can be used as part of decision trees or other algorithms.  */
class AbsoluteDecider private constructor(
    /** The language to always pick  */
    private val language: SingleLanguageDecision
) : Decider {
    override fun decide(row: InputRow): LanguageDecision {
        return language
    }

    override fun representation(numSpaces: Int): String {
        return "return $language, 1.0 confidence"
    }

    companion object {
        private val deciders = EnumMap<Language, AbsoluteDecider>(
            Language::class.java
        )

        /** Returns the decider to always decide input is the language specified.
         * Uses a cache to reduce object creations.  */
		@JvmStatic
		fun fromLanguage(language: Language): AbsoluteDecider {
            if (!deciders.containsKey(language)) {
                deciders[language] = AbsoluteDecider(SingleLanguageDecision.fromLanguage(language))
            }
            return deciders[language]!!
        }
    }
}
