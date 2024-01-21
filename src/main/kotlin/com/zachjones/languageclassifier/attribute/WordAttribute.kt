package com.zachjones.languageclassifier.attribute

import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.model.types.Language
import kotlin.random.Random

/***
 * Represents an attribute that is based solely on a word from the input matching this word.
 */
class WordAttribute(
        private val word: String,
        inputs: List<InputRow>,
        languageOne: Language
) : Attribute() {
    override val fitness: Double = fitness(this, inputs, languageOne)

    override fun has(input: InputRow): Boolean {
        // iterate through the words to see if one matches exactly
        for (i in input.words.indices) {
            if (input.words[i] == word) return true
        }
        return false
    }

    override fun name(): String {
        return "a word is '$word'"
    }

    override fun mutate(words: List<String>, inputs: List<InputRow>, languageOne: Language, languageTwo: Language): Attribute {
        // pick a new word from the list, since they are all there, picking a random one will more likely be a good guess
        val index = Random.nextInt(words.size)
        return WordAttribute(words[index], inputs, languageOne)
    }
}
