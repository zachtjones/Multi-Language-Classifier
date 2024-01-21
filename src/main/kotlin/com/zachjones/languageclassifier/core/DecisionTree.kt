package com.zachjones.languageclassifier.core

import com.zachjones.languageclassifier.attribute.Attribute
import com.zachjones.languageclassifier.core.AbsoluteDecider.Companion.fromLanguage
import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.entities.LanguageDecision
import com.zachjones.languageclassifier.entities.WeightedList
import com.zachjones.languageclassifier.entities.spaces
import com.zachjones.languageclassifier.model.types.Language
import java.util.function.Predicate
import kotlin.math.ln

class DecisionTree
/** Creates a decision tree given the left and right decisions, and the index of the attributes it splits on.  */ private constructor(// could be a tree or a -- if false, this is evaluated
    private val left: Decider, // could be a tree of b -- if true, this is evaluated
    private val right: Decider, // attribute to split on
    private val splitOn: Attribute
) : Decider {
    /** Instance method to make the decision on input.  */
    override fun decide(row: InputRow): LanguageDecision {
        // recursive, test the inputs on the left and right side
        return if (splitOn.has(row)) right.decide(row) else left.decide(row)
    }

    /** How this should be represented as a string.  */
    override fun representation(numSpaces: Int): String {
        // I'm making it like a code block.
        return """if ${splitOn.name()} then: 
${(numSpaces + 2).spaces()}${right.representation(numSpaces + 2)}
${numSpaces.spaces()}else: 
${(numSpaces + 2).spaces()}${left.representation(numSpaces + 2)}"""
    }

    companion object {
        private fun languageIs(language: Language): Predicate<Pair<Double, InputRow>> {
            return Predicate { i: Pair<Double, InputRow> -> i.second.language == language }
        }

        /**
         * Picks the best decision tree to represent the decision at this level, with the options left.
         * This is a binary classifier of the two values provided.
         * Precondition:
         * all data is either languageOne or languageTwo
         * @param allData The weighted data this tree should decide on. (portion of the original data)
         * @param levelLeft The amount of tree depth left, if this is 0, then it will be decide always the one output.
         * @param optionsLeft The options that are left to choose from to split on.
         * @param totalSize The total size of the original data, not just what this has to choose from.
         * @param languageOne The first language
         * @param languageTwo The second language
         * @return A learned decision tree.
         */
        fun learn(
            allData: WeightedList<InputRow>, levelLeft: Int, optionsLeft: Set<Attribute>,
            totalSize: Int, languageOne: Language, languageTwo: Language?
        ): Decider {
            require(allData.size() != 0) { "All data size should not be 0" }

            // count how many have output LanguageOne
            val countOne = allData.stream().filter(languageIs(languageOne)).count()

            // base case 1: there is no levels left to iterate - make a definite decision.
            if (levelLeft == 0) {
                // return the majority by total weight
                val weightOne = allData.stream().filter(languageIs(languageOne))
                    .mapToDouble { i: Pair<Double, InputRow> -> i.first }
                    .sum()
                val totalWeight = allData.totalWeight()

                return ConfidenceDecider(languageOne, languageTwo!!, weightOne / totalWeight)
            }

            // base case 2: there is all of one type
            if (allData.size().toLong() == countOne) { // all are the first one
                return fromLanguage(languageOne)
            } else if (countOne == 0L) { // none are the first one, they are all the second
                return fromLanguage(languageTwo!!)
            }

            // split on the best attribute - based on information gain
            val result = optionsLeft.stream()
                .map { i: Attribute ->
                    Pair(
                        i,
                        entropyForDecision(i, allData, totalSize, languageOne)
                    )
                } // pair<att, entropy>
                .min(Comparator.comparingDouble { pair: Pair<Attribute, Double> -> pair.second })

            // result should be present -- if not we still have depth left but have exhausted options
            if (result.isEmpty) {
                // ran out of options, just return the majority
                return learn(allData, 0, emptySet(), totalSize, languageOne, languageTwo)
            }
            val best = result.get().first

            val trueOnes = allData.valuesWith { input: InputRow? ->
                best.has(
                    input!!
                )
            }
            val falseOnes = allData.valuesWith { input: InputRow? ->
                best.doesntHave(
                    input!!
                )
            }
            // need copies so they don't modify each other
            val optionsLeftFalse: MutableSet<Attribute> = HashSet(optionsLeft)
            optionsLeftFalse.remove(best)
            val optionsLeftTrue: MutableSet<Attribute> = HashSet(optionsLeft)
            optionsLeftTrue.remove(best)

            // single recursive where this attribute does not help
            if (trueOnes.size() == 0) {
                return learn(falseOnes, levelLeft, optionsLeftFalse, totalSize, languageOne, languageTwo)
            }
            if (falseOnes.size() == 0) {
                return learn(trueOnes, levelLeft, optionsLeftTrue, totalSize, languageOne, languageTwo)
            }

            // do the recursive calls
            val left = learn(falseOnes, levelLeft - 1, optionsLeftFalse, totalSize, languageOne, languageTwo)
            val right = learn(trueOnes, levelLeft - 1, optionsLeftTrue, totalSize, languageOne, languageTwo)

            // return the created tree
            return DecisionTree(left, right, best)
        }

        /** Returns the entropy associated with a decision to split on attribute index.  */
        private fun entropyForDecision(
            attribute: Attribute,
            allData: WeightedList<InputRow>,
            totalSize: Int,
            languageOne: Language
        ): Double {
            // split into the two sets - calculate entropy on each
            val falseOnes = allData.valuesWith { input: InputRow? ->
                attribute.doesntHave(
                    input!!
                )
            }
            val trueOnes = allData.valuesWith { input: InputRow? ->
                attribute.has(
                    input!!
                )
            }

            // entropy for each half * the proportion of the whole it is.
            return trueOnes.size().toDouble() / totalSize * entropyForList(trueOnes, languageOne) +
                    falseOnes.size().toDouble() / totalSize * entropyForList(falseOnes, languageOne)
        }

        /** Returns the entropy for a list of data in bits.  */
        private fun entropyForList(allData: WeightedList<InputRow>, languageOne: Language): Double {
            // if there's no data, entropy is 0
            if (allData.size() == 0) return 0.0

            val total = allData.totalWeight()
            // arbitrary - defined first to be true (doesn't matter since symmetric distribution).
            val eAmount = allData.valuesWith { i: InputRow -> i.language == languageOne }.totalWeight()

            return entropyForBoolean(eAmount / total)
        }

        /** Returns the entropy for a weighted boolean random variable.  */
		@JvmStatic
		fun entropyForBoolean(trueChance: Double): Double {
            // 0 or 1 will result in NaN, but it should be 0.
            if (trueChance == 0.0 || trueChance == 1.0) {
                return 0.0
            }
            // from the book −(q log2 q + (1 − q) log2(1 − q))
            val falseChance = 1 - trueChance
            return -(trueChance * log2(trueChance) + falseChance * log2(falseChance))
        }

        /** Returns the log base two of a number.  */
        fun log2(x: Double): Double {
            // log2 not defined in java in the Math class.
            return ln(x) / ln(2.0)
        }
    }
}
