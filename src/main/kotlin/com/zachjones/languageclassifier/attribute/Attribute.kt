package com.zachjones.languageclassifier.attribute

import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.model.types.Language
import java.io.Serializable
import kotlin.math.max

abstract class Attribute : Serializable, Comparable<Attribute> {
    override fun compareTo(other: Attribute): Int {
        // sort by the fitness, then name to split ties (don't want to attributes that are equivalent)
        val result = this.fitness.compareTo(other.fitness)
        return if (result == 0) {
            name().compareTo(other.name())
        } else {
            result
        }
    }

    /** Method to determine if the attribute is true on this input row.  */
    abstract fun has(input: InputRow): Boolean

    /** A human-readable name for this attribute.  */
    abstract fun name(): String

    /** Returns the name. This way objects are required to implement it  */
    override fun toString(): String {
        return name()
    }

    /** Helper method to make code elsewhere more concise.  */
    fun doesntHave(input: InputRow): Boolean {
        return !has(input)
    }

    /** returns the fitness cached  */
	abstract val fitness: Double

    /**
     * Returning another attribute that is based on this, but slightly different.
     * @param words A list of words to use for the mutation to be a representative sample.
     * @return The new attribute, which is a mutation of this.
     */
    abstract fun mutate(words: List<String>, inputs: List<InputRow>, languageOne: Language, languageTwo: Language): Attribute

    companion object {
        /***
         * Calculates the fitness of this attribute on a scale of 0 to 1.0, based on the percent.
         * Precondition: all the elements are one of the two languages.
         * @param inputs The list of inputs that are used to test the attribute
         * @param languageOne The first language to test
         * @return The fitness calculation.
         */
        fun fitness(thing: Attribute, inputs: List<InputRow>, languageOne: Language): Double {
            // could see the best decision tree that could be learned from this attribute,
            //   but it's faster to do this way since there's a lot of simplification that can be done:
            // One: the inputs are always unweighted
            // Two: the tree is only 1 deep
            //   - don't have to iterate through all 3x, and create the two sub-lists to handle each definite decision

            var countHasLangOne = 0
            var countHasLangTwo = 0
            var countNoLangOne = 0
            var countNoLangTwo = 0

            for (r in inputs) {
                if (thing.has(r)) {
                    if (r.language == languageOne) countHasLangOne++
                    else countHasLangTwo++
                } else {
                    if (r.language == languageOne) countNoLangOne++
                    else countNoLangTwo++
                }
            }

            // for each part, pick the larger side -- what the decision tree would do
            val correctHas = max(countHasLangOne.toDouble(), countHasLangTwo.toDouble())
            val correctNo = max(countNoLangOne.toDouble(), countNoLangTwo.toDouble())

            return (correctHas + correctNo) / (countHasLangOne + countHasLangTwo + countNoLangOne + countNoLangTwo)
        }
    }
}
