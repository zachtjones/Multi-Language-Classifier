package com.zachjones.languageclassifier.attribute

import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.model.types.Language
import java.util.Random
import java.util.stream.Collectors

class WordStartingAttribute(
        private val start: String,
        inputs: List<InputRow>,
        languageOne: Language
) : Attribute() {
    override val fitness: Double = fitness(this, inputs, languageOne)

    override fun has(input: InputRow): Boolean {
        // iterate through the words to see if one matches exactly
        for (i in input.words.indices) {
            if (input.words[i].startsWith(start)) return true
        }
        return false
    }

    override fun name(): String {
        return "a word starts with '$start'"
    }

    override fun mutate(words: List<String>, inputs: List<InputRow>, languageOne: Language, languageTwo: Language): Attribute {
        val r = Random()

        // either replace the last character, or append a new character
        // replace the characters only 1/4 of the time
        val newStarting = if (r.nextBoolean() && r.nextBoolean() && start.length > 0) {
            // replace the last letter of the suffix, so you drop the last character
            start.substring(0, start.length - 1)
        } else {
            // appending a new character
            start
        }

        // add another letter on to the suffix, drawn from the pool words with the suffix passing
        val wordsFilter = words.stream()
                .filter { i: String -> i.startsWith(newStarting) }
                .collect(Collectors.toList())

        // nothing passed the filter, that's a bad start, don't want to keep it
        if (wordsFilter.size == 0) return this

        // draw a random one from the list
        val random = wordsFilter[r.nextInt(wordsFilter.size)]

        val rest = random.substring(newStarting.length)
        if (rest.length > 1) {
            val extra = rest[0]
            return WordStartingAttribute(newStarting + extra, inputs, languageOne)
        }
        // if there weren't any extra characters left on the word, just return this
        return this
    }
}
