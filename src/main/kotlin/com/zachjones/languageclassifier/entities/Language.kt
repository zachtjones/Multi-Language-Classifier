package com.zachjones.languageclassifier.entities

import com.zachjones.languageclassifier.model.types.Language

/**
 * Converts this string constant to the Language value.
 */
fun String.toLanguage(): Language = Language.values().first { it.name.equals(this, ignoreCase = true) }

private val languageValues = Language.values()

/**
 * List of pairs of languages, such that if there are languages 1, 2, 3, the list will be:
 * (1, 2)
 * (1, 3)
 * (2, 3)
 */
val languagePairs: List<helper.Pair<Language, Language>> = languageValues.flatMapIndexed { index, first ->
    val items = mutableListOf<helper.Pair<Language, Language>>()
    for (j in index + 1 until languageValues.size) {
        items += helper.Pair(first, languageValues[j])
    }
    items
}

