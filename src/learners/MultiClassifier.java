package learners;

import helper.Pair;
import helper.WeightedList;
import attributes.Attributes;
import helper.MultiLanguageDecision;
import main.InputRow;
import main.Learning;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class MultiClassifier implements Serializable, Decider {

	/** Holds a list of all the binary classifiers. */
	private List<Decider> deciders;


	private MultiClassifier(List<Decider> deciders) {
		this.deciders = deciders;
	}

	/**
	 * Learns a decision tree with the data provided and the depth.
	 * First figures out the attributes for each pair of languages, and then learns a decision tree on each.
	 * @param rows The List of labeled input.
	 * @param depth The depth of the trees.
	 * @param numberGenerations The number of generations on the genetic algorithm
	 * @param poolSize The number of attributes to keep in the pool
	 * @param printBinaryAccuracy Whether to print out the accuracy of the binary deciders or not
	 * @return A multi-classifier based on simple decision trees for each language pair.
	 */
	public static MultiClassifier learnDecisionTree(List<InputRow> rows, int depth,
													int numberGenerations, int poolSize, boolean printBinaryAccuracy) {

		return learn(rows, true, depth, numberGenerations, poolSize, printBinaryAccuracy);
	}

	/**
	 * Learns an Adaptive Boosting ensemble, composed of trees, eacb of depth 1.
	 * First figures out the attributes for each pair of languages, and then learns a decision tree on each.
	 * @param rows The List of labeled input.
	 * @param ensembleSize The number of decision stumps in the ensemble.
	 * @param numberGenerations The number of generations on the genetic algorithm
	 * @param poolSize The number of attributes to keep in the pool
	 * @param printBinaryAccuracy Whether to print out the accuracy of the binary deciders or not
	 * @return A multi-classifier based on the AdaBoost ensemble for each language pair.
	 */
	public static MultiClassifier learnAdaBoost(List<InputRow> rows, int ensembleSize,
												int numberGenerations, int poolSize, boolean printBinaryAccuracy) {

		return learn(rows, false, ensembleSize, numberGenerations, poolSize, printBinaryAccuracy);
	}

	private static MultiClassifier learn(List<InputRow> rows, boolean isDecisionTree, int param,
										 int numberGenerations, int poolSize, boolean printBinaryAccuracy) {
		List<Pair<String, String>> languagePairs = Learning.languagePairs;

		// each sub-problem: learning to distinguish a pair of languages
		//   - can run each of these sub-problems in parallel
		List<Decider> allTrees = languagePairs.parallelStream().map(pair -> {
			String first = pair.one;
			String second = pair.two;

			List<InputRow> twoLanguages = rows.stream()
				.filter(i -> i.outputValue.equals(first) || i.outputValue.equals(second))
				.collect(Collectors.toList());

			Set<Attributes> attributes =
				GeneticLearning.learnAttributes(twoLanguages, first, second, numberGenerations, poolSize);

			// learn a decision tree based on the attributes, with depth
			final Decider binaryDecider;
			if (isDecisionTree) {
				binaryDecider = DecisionTree.learn(
					new WeightedList<>(twoLanguages), param, attributes, twoLanguages.size(), first, second
				);
			} else {
				binaryDecider = Adaboost.learn(
					new WeightedList<>(twoLanguages), param, attributes, first, second
				);
			}

			// determine accuracy
			if (printBinaryAccuracy) {
				// each line is important to not be split, have to synchronize on system.out
				synchronized (System.out) {
					System.out.print("Binary classifier training accuracy (" + first + " vs " + second + "): ");
					System.out.println(100 * (1 - binaryDecider.errorRateUnWeighted(twoLanguages)));
				}
			}
			return binaryDecider;

		}).collect(Collectors.toList());

		return new MultiClassifier(allTrees);
	}

	@Override
	public LanguageDecision decide(InputRow row) {
		// return a language decision based on the combined confidence of each decider
		MultiLanguageDecision decision = new MultiLanguageDecision();

		// add on the confidences -- when it's near 50/50 for the binary decider, count less
		for (Decider i : deciders) {
			// base on the entropy of the boolean random variable for the most common
			LanguageDecision d = i.decide(row);
			double confidence = d.confidenceForLanguage(d.mostConfidentLanguage());
			double relativeWeight = 1 - DecisionTree.entropyForBoolean(confidence);
			decision.addWeightTo(i.decide(row), relativeWeight);
		}

		decision.normalize();

		return decision;
	}

	@Override
	public String representation(int numSpaces) {
		return "Learn using the following deciders:\n" +
			deciders.stream()
				.map(i -> i.representation(numSpaces))
				.collect(Collectors.joining("\n"));
	}
}
