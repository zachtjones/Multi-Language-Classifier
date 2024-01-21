package com.zachjones.languageclassifier.attribute

import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.model.types.Language
import java.util.Random
import java.util.stream.Collectors

class WordContainsAttribute(
        private val contains: String,
        inputs: List<InputRow>,
        languageOne: Language
) : Attribute() {
    override val fitness: Double = fitness(this, inputs, languageOne)

    override fun has(input: InputRow): Boolean {
        // iterate through the words to see if one matches exactly
        for (i in input.words.indices) {
            if (input.words[i].contains(contains)) return true
        }
        return false
    }

    override fun name(): String {
        return "a word contains '$contains'"
    }

    override fun mutate(words: List<String>, inputs: List<InputRow>, languageOne: Language, languageTwo: Language): Attribute {
        val r = Random()

        // either replace the last character, or append a new character
        // replace the characters only 1/4 of the time
        val newContains = if (r.nextBoolean() && r.nextBoolean() && contains.isNotEmpty()) {
            // drop either the starting character or the ending character
            if (r.nextBoolean()) {
                contains.substring(1)
            } else {
                contains.substring(0, contains.length - 1)
            }
        } else {
            // appending a new character, either beginning or the end (decided later)
            contains
        }

        // add another letter on to the suffix, drawn from the pool words with the suffix passing
        val wordsFilter = words.stream()
                .filter { i: String -> i.contains(newContains) }
                .collect(Collectors.toList())

        // nothing passed the filter, that's a bad start, don't want to keep it
        if (wordsFilter.size == 0) return this

        // draw a random one from the list
        val random = wordsFilter[r.nextInt(wordsFilter.size)]

        // pick either: append or prepend another character, drawn from random word
        if (r.nextBoolean()) {
            // prepend the starting

            val index = random.indexOf(newContains)
            val starting = random.substring(0, index)

            if (starting.isNotEmpty()) {
                // get the last character of what's before the random word
                val newChar = starting[starting.length - 1]
                return WordContainsAttribute(newChar.toString() + newContains, inputs, languageOne)
            }
            // word contains the pattern at the start of the word, return this
            return this
        } else {
            // append the ending

            val ending = random.substring(random.indexOf(newContains) + newContains.length)

            if (ending.isNotEmpty()) {
                // get the first character of what's after the random word
                val newChar = ending[0]
                return WordContainsAttribute(newContains + newChar, inputs, languageOne)
            }
            // word contains the pattern at the end of the word, return this
            return this
        }
    }
}
