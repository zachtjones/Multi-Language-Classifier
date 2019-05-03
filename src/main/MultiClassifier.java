package main;

import helper.Pair;
import helper.WeightedList;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class MultiClassifier implements Serializable, Decider {

	/** Holds a list of all the binary classifiers. */
	private List<Decider> allTrees;


	private MultiClassifier(List<Decider> allTrees) {
		this.allTrees = allTrees;
	}

	/**
	 * Learns a decision tree with the data provided and the depth.
	 * First figures out the attributes for each pair of languages, and then learns a decision tree on each.
	 * @param rows The List of labeled input.
	 * @param depth The depth of the trees.
	 * @return A multi-classifier based on simple decision trees.
	 */
	static MultiClassifier learnDecisionTree(List<InputRow> rows, int depth) {
		List<Pair<String, String>> languagePairs = Learning.languagePairs;

		List<Decider> allTrees = new ArrayList<>();

		for (Pair<String, String> pair : languagePairs) {

			String first = pair.one;
			String second = pair.two;

			System.out.println("\nLearning binary classifier between " + first + " and " + second);

			// TODO see if a copy and remove if is faster, but first see which part is slow

			List<InputRow> twoLanguages = rows.stream()
				.filter(i -> i.outputValue.equals(first) || i.outputValue.equals(second))
				.collect(Collectors.toList());

			// TODO make number of generations and pool size a parameter
			Set<Attributes> attributes =
				GeneticLearning.learnAttributes(twoLanguages, first, second, 50, 12);

			// learn a decision tree based on the attributes, with depth
			Decider newTree = DecisionTree.learn(
				new WeightedList<>(twoLanguages), depth, attributes, twoLanguages.size(), first, second
			);

			// print out the new tree
			System.out.println("Learned:\n" + newTree.representation(0));

			// determine accuracy
			System.out.print("Accuracy of binary classification: ");
			System.out.println(100 * (1 - newTree.errorRateUnWeighted(twoLanguages)));

			allTrees.add(newTree);
		}

		return new MultiClassifier(allTrees);
	}

	@Override
	public String decide(InputRow row) {
		// base on the most number of +1's
		Map<String, Long> counts = allTrees.stream()
			.map(i -> i.decide(row)) // stream of decisions
			.collect(Collectors.groupingBy(i -> i, Collectors.counting())); // map< language, count

		// find most common one
		return counts.entrySet().stream()
			.max(Comparator.comparingLong(Map.Entry::getValue)) // extract max pair
			.map(Map.Entry::getKey).orElse(null);
	}

	@Override
	public String representation(int numSpaces) {
		return "Learn using the following deciders:\n" +
			allTrees.stream()
				.map(i -> i.representation(numSpaces))
				.collect(Collectors.joining("\n"));
	}
}
