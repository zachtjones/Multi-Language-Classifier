package com.zachjones.languageclassifier.entities

import com.zachjones.languageclassifier.model.types.Language

const val DATA_PATH = "data/"
const val TRAINING_DATA_PREFIX = "training-"
const val TRAINING_DATA_SUFFIX = ".json"

const val MODEL_PREFIX = "model-"
const val MODEL_SUFFIX = ".dat"

val WORD_SPLIT_REGEX = Regex("[\\-()*&^%$#@!,./?\";:+«»‘\\[\\]{}=_\\\\|\u8211\u0183°′”″“’ʻ·–—•º„]")


fun poemSourceFile(language: Language) = "${DATA_PATH}odyssey-${language.name.lowercase()}.txt"

/** Creates a string of length this comprised of spaces.  */
fun Int.spaces(): String = " ".repeat(this)

fun List<InputRow>.averageWordCount(): Double = this.map { it.words.size.toDouble() }.average()