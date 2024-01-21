package com.zachjones.languageclassifier.core

import com.zachjones.languageclassifier.entities.InputRow
import com.zachjones.languageclassifier.entities.LanguageDecision
import com.zachjones.languageclassifier.entities.MultiLanguageDecision
import com.zachjones.languageclassifier.entities.WeightedList
import com.zachjones.languageclassifier.model.types.Language
import com.zachjones.languageclassifier.model.types.ModelType
import java.io.Serializable
import java.util.stream.Collectors

class MultiClassifier private constructor(
    /** Holds a list of all the binary classifiers.  */
    private val deciders: List<Decider>, val description: String
) : Serializable, Decider {
    override fun decide(row: InputRow): LanguageDecision {
        // return a language decision based on the combined confidence of each decider
        val decisions = deciders.map { it.decide(row) }
        return MultiLanguageDecision.of(decisions)
    }

    override fun representation(numSpaces: Int): String {
        return "Learn using the following deciders:\n" +
                deciders.stream()
                    .map { i: Decider -> i.representation(numSpaces) }
                    .collect(Collectors.joining("\n"))
    }

    companion object {
        /**
         * Learns a decision tree with the data provided and the depth.
         * First figures out the attributes for each pair of languages, and then learns a decision tree on each.
         * @param rows The List of labeled input.
         * @param depth The depth of the trees.
         * @param numberGenerations The number of generations on the genetic algorithm
         * @param poolSize The number of attributes to keep in the pool
         * @return A multi-classifier based on simple decision trees for each language pair.
         */
        fun learnDecisionTree(
            rows: List<InputRow>, depth: Int,
            numberGenerations: Int, poolSize: Int
        ): MultiClassifier {
            return learn(rows, ModelType.DECISION_TREE, numberGenerations, poolSize, depth)
        }

        /**
         * Learns an Adaptive Boosting ensemble, composed of trees, each of depth 1.
         * First figures out the attributes for each pair of languages, and then learns a decision tree on each.
         * @param rows The List of labeled input.
         * @param ensembleSize The number of decision stumps in the ensemble.
         * @param numberGenerations The number of generations on the genetic algorithm
         * @param poolSize The number of attributes to keep in the pool
         * @return A multi-classifier based on the AdaBoost ensemble for each language pair.
         */
        fun learnAdaBoost(
            rows: List<InputRow>, ensembleSize: Int,
            numberGenerations: Int, poolSize: Int
        ): MultiClassifier {
            return learn(rows, ModelType.ADAPTIVE_BOOSTING_TREE, numberGenerations, poolSize, ensembleSize)
        }


        private fun learn(
            rows: List<InputRow>, method: ModelType, numberGenerations: Int,
            poolSize: Int, param: Int
        ): MultiClassifier {
            var description = "Model using " + rows.size +
                    " total phrases, method=" + method +
                    ", attributeGenerations=" + numberGenerations +
                    ", attributePoolSize=" + poolSize
            description += if (method == ModelType.DECISION_TREE) {
                ", treeDepth=$param"
            } else {
                ", ensembleSize=$param"
            }

            // each sub-problem: learning to distinguish a pair of languages
            //   - can run each of these sub-problems in parallel
            val allTrees = Language.values().filter { it != Language.OTHER }.parallelStream().map { language ->

                // train as if it is 1 language vs others -- take random examples from the other side
                val thisLanguageExamples = rows.filter { it.language == language }
                val otherLanguageExamples = rows.filter { it.language != language }
                    .map { it.copy(language = Language.OTHER) }
                    .shuffled()
                    .take(thisLanguageExamples.size)

                val allExamples = thisLanguageExamples + otherLanguageExamples

                val attributes = GeneticLearning.learnAttributes(allExamples, language, Language.OTHER, numberGenerations, poolSize)

                // learn a decision tree based on the attributes, with depth
                val binaryDecider = when (method) {
                    ModelType.DECISION_TREE -> {
                        DecisionTree.learn(
                            allData = WeightedList(allExamples),
                            levelLeft = param,
                            optionsLeft = attributes,
                            totalSize = allExamples.size,
                            languageOne = language,
                            languageTwo = Language.OTHER
                        )
                    }
                    ModelType.ADAPTIVE_BOOSTING_TREE -> {
                        Adaboost.learn(
                            inputs = WeightedList(allExamples),
                            ensembleSize = param,
                            attributes = attributes,
                            languageOne = language,
                            languageTwo = Language.OTHER
                        )
                    }
                }

                // determine accuracy
                // each line is important to not be split, have to synchronize on system.out
                synchronized(System.out) {
                    print("Binary classifier training accuracy ($language vs ${Language.OTHER}): ")
                    println(100 * (1 - binaryDecider.errorRateUnWeighted(allExamples)))
                }
                binaryDecider
            }.collect(Collectors.toList())

            return MultiClassifier(allTrees, description)
        }
    }
}
