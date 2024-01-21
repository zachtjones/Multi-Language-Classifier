package com.zachjones.languageclassifier.core

import com.zachjones.languageclassifier.attribute.Attribute
import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.entities.LanguageDecision
import com.zachjones.languageclassifier.entities.MultiLanguageDecision
import com.zachjones.languageclassifier.entities.WeightedList
import com.zachjones.languageclassifier.model.types.Language
import java.util.stream.Collectors

class Adaboost private constructor(input: WeightedList<InputRow>) : Decider {
    /** weighted inputs and hypotheses used in learning.
     * These weights change as we go through.  */
    private var inputs: WeightedList<InputRow>

    /** This one is the weights according to the correct % of each hypothesis.
     * We need to keep this not normalized and then do the normalization at the end of each iteration.  */
    private val hypotheses = WeightedList<Decider>()

    /** This list will always be a normalized list of the individual stumps.  */
    private var normalizedHypotheses: WeightedList<Decider>? = null

    /** Private constructor to create an Adaboost ensemble  */
    init {
        this.inputs = input
    }

    /** Represents the ensemble's decision on the input row.  */
    override fun decide(row: InputRow): LanguageDecision {
        // map the first language to -1, and the second language to 1

        val weights = WeightedList<LanguageDecision>()

        for (i in 0 until normalizedHypotheses!!.size()) {
            // iterate through the hypotheses, weighting their predictions

            val weight = normalizedHypotheses!!.getWeight(i)
            weights.addWeight(weight, normalizedHypotheses!!.getItem(i).decide(row))
        }

        weights.normalize()

        val mapWeights = mutableMapOf<Language, Double>()
        Language.values().forEach { mapWeights[it] = 0.0 }
        weights.stream().forEach {(decisionConfidence, languageDecision) ->
            languageDecision.confidences().forEach { (language, languageConfidence) ->
                mapWeights[language] = mapWeights[language]!! + (languageConfidence * decisionConfidence)
            }
        }
        return MultiLanguageDecision(weights = mapWeights)
    }

    override fun representation(numSpaces: Int): String {
        val rep = "Adaboost on the following " + hypotheses.size() + " decision stumps:\n"

        val each = normalizedHypotheses!!.stream()
            .map { i: Pair<Double, Decider> ->
                """
     Weight: ${i.first}, stump:
     ${i.second.representation(0)}
     """.trimIndent()
            }
            .collect(Collectors.joining("\n\n"))

        return """
            $rep
            $each
            """.trimIndent()
    }

    companion object {
        /** Learns using the Adaboost technique on decision stumps.
         * This learns a binary classifier between the two languages.
         * @param inputs The inputs to decide on. Should initially be unweighted.
         * @param ensembleSize The number of stumps to create.
         * @param attributes The set of potential attributes to use.
         * @param languageOne The first language of the inputs
         * @param languageTwo The second language of the inputs
         */
        fun learn(
            inputs: WeightedList<InputRow>, ensembleSize: Int, attributes: Set<Attribute>,
            languageOne: Language, languageTwo: Language
        ): Decider {
            val result = Adaboost(inputs)
            val totalSize = inputs.size()

            for (i in 0 until ensembleSize) {
                val newHypothesis =
                    DecisionTree.learn(result.inputs, 1, attributes, totalSize, languageOne, languageTwo)

                var error = 0.0
                for (j in 0 until inputs.size()) {
                    val row = inputs[j] // weight, example
                    if (newHypothesis.decide(row.second).mostConfidentLanguage() != row.second.language) {
                        error += row.first
                    }
                }

                // special case: we learn it 100%, should just add it with normal weight
                if (error == 0.0) {
                    result.hypotheses.addWeight(1.0, newHypothesis)
                    result.normalizedHypotheses = result.hypotheses.normalize()
                    break
                }

                // reduce the weights of the ones we got right
                val reductionRate = error / (1 - error)
                for (j in 0 until result.inputs.size()) {
                    val row = result.inputs[j]
                    if (newHypothesis.decide(row.second).mostConfidentLanguage() == row.second.language) {
                        result.inputs.setWeight(j, result.inputs.getWeight(j) * reductionRate)
                    }
                }

                // normalize weights
                result.inputs = result.inputs.normalize()

                // calculate the weight for this hypothesis and add it
                result.hypotheses.addWeight(DecisionTree.log2((1 - error) / error), newHypothesis)
                result.normalizedHypotheses = result.hypotheses.normalize()
            }
            return result
        }
    }
}
