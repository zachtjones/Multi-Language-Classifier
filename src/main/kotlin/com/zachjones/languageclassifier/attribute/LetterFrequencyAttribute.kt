package com.zachjones.languageclassifier.attribute

import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.model.types.Language
import java.util.Random

/**
 * These attributes split the results based on letter frequency of one letter more than another one.
 * @param more The letter that the sentence has more of
 * @param less The letter that the sentence has less of
 */
class LetterFrequencyAttribute(
        private val more: Char,
        private val less: Char,
        inputs: List<InputRow>,
        languageOne: Language
) : Attribute() {
    override val fitness: Double = fitness(this, inputs, languageOne)

    override fun has(input: InputRow): Boolean {
        var moreCount = 0
        var lessCount = 0

        for (i in input.words.indices) {
            val wordI = input.words[i]
            for (element in wordI) {
                if (element == more) moreCount++
                if (element == less) lessCount++
            }
        }

        return moreCount > lessCount
    }

    override fun name(): String {
        return "words have more '$more' than '$less'"
    }

    override fun mutate(words: List<String>, inputs: List<InputRow>, languageOne: Language, languageTwo: Language): Attribute {
        // pick a random letter of a random word and replace one of this fields with it
        val r = Random()
        val word = words[r.nextInt(words.size)]
        val letter = word[r.nextInt(word.length)]
        return if (r.nextBoolean()) {
            LetterFrequencyAttribute(this.more, letter, inputs, languageOne)
        } else {
            LetterFrequencyAttribute(letter, this.less, inputs, languageOne)
        }
    }
}
