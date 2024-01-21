package com.zachjones.languageclassifier.core

import com.zachjones.languageclassifier.attribute.Attribute
import com.zachjones.languageclassifier.attribute.LetterFrequencyAttribute
import com.zachjones.languageclassifier.attribute.WordAttribute
import com.zachjones.languageclassifier.attribute.WordContainsAttribute
import com.zachjones.languageclassifier.attribute.WordEndingAttribute
import com.zachjones.languageclassifier.attribute.WordStartingAttribute
import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.model.types.Language
import java.util.Random
import java.util.TreeSet

class GeneticLearning private constructor(
    /** Holds the input values list  */
    private val inputs: List<InputRow>,
    /** The two languages to split between  */
    private val languageOne: Language, private val languageTwo: Language
) {
    /** Holds the pool of potential attributes  */

    // pool is sorted by the fitness of the feature
    private val pool = TreeSet<Attribute>()

    /** Holds a list of all the words that are in the input.  */
    private val allWords: MutableList<String> = ArrayList(inputs.size * 20)

    /** Random used for chances with mutation possibilities  */
    private val r = Random()

    /**
     * Creates the learning pool with a genetic algorithm.
     * @param inputs The input rows to use as part of the measure for fitness
     * @param languageOne The first language.
     * @param languageTwo The second language.
     */
    init {
        for ((_, words) in inputs) {
            allWords.addAll(words)
        }

        // fill in the pool with some randomly drawn attributes
        val noUse: Attribute = WordAttribute("a", inputs, languageOne)
        for (i in 0..19) {
            pool.add(noUse.mutate(allWords, inputs, languageOne, languageTwo))
        }

        // letter frequency
        val noUseLetters: Attribute = LetterFrequencyAttribute('a', 'z', inputs, languageOne)
        for (i in 0..19) {
            pool.add(noUseLetters.mutate(allWords, inputs, languageOne, languageTwo))
        }

        // word endings
        val noUseEnding: Attribute = WordEndingAttribute("", inputs, languageOne)
        for (i in 0..19) {
            pool.add(noUseEnding.mutate(allWords, inputs, languageOne, languageTwo))
        }

        // word starting
        val noUseStarting: Attribute = WordStartingAttribute("", inputs, languageOne)
        for (i in 0..19) {
            pool.add(noUseStarting.mutate(allWords, inputs, languageOne, languageTwo))
        }

        // word contains
        val noUseContaining: Attribute = WordContainsAttribute("", inputs, languageOne)
        for (i in 0..19) {
            pool.add(noUseContaining.mutate(allWords, inputs, languageOne, languageTwo))
        }
    }

    /**
     * Proceeds to the next generation, performing mutation, crossover, and then trimming
     * the results down to the pool size, keeping the most fit.
     */
    private fun nextGeneration(maxPoolSize: Int) {
        // mutate some random ones, proportional to their fitness
        val newOnes = TreeSet<Attribute>()

        for (i in pool) {
            val chance = i.fitness
            if (r.nextDouble() < chance) {
                // do the mutation and add it to the pool
                newOnes.add(i.mutate(allWords, inputs, languageOne, languageTwo))
            }
        }

        // TODO add the crossover once that's implemented

        // add the new ones to the list
        pool.addAll(newOnes)

        // trim the pool to MAX_POOL_SIZE (aka natural selection in the genetic sense)
        while (pool.size > maxPoolSize) {
            pool.pollFirst()
        }
    }

    companion object {
        /***
         * Learns a set of attributes from the input data, given a genetic algorithm.
         * Precondition: all examples are either language one or two.
         * @param inputs The labeled data to learn from (contains all examples, not just languageOne and two).
         * @param languageOne The first language.
         * @param languageTwo The second language.
         * @param numberGenerations The number of generations to run
         * @param poolSize The pool size of attributes to retain.
         * @return The set of attributes learned.
         */
        fun learnAttributes(
            inputs: List<InputRow>, languageOne: Language, languageTwo: Language,
            numberGenerations: Int, poolSize: Int
        ): Set<Attribute> {
            // iterate 100 generations
            val learning = GeneticLearning(inputs, languageOne, languageTwo)
            for (i in 0 until numberGenerations) {
                learning.nextGeneration(poolSize)
            }

            return learning.pool
        }
    }
}
