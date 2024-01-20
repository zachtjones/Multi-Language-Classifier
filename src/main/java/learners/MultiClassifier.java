package learners;

import com.zachjones.languageclassifier.attribute.Attribute;
import com.zachjones.languageclassifier.entities.InputRow;
import com.zachjones.languageclassifier.entities.LanguageDecision;
import com.zachjones.languageclassifier.entities.MultiLanguageDecision;
import com.zachjones.languageclassifier.model.types.Language;
import helper.WeightedList;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zachjones.languageclassifier.entities.LanguageKt.getLanguagePairs;

public class MultiClassifier implements Serializable, Decider {

	/** Holds a list of all the binary classifiers. */
	private final List<Decider> deciders;

	private final String description;


	private MultiClassifier(List<Decider> deciders, String description) {
		this.deciders = deciders;
		this.description = description;
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

		return learn(rows, "decision", numberGenerations, poolSize, printBinaryAccuracy, depth);
	}

	/**
	 * Learns an Adaptive Boosting ensemble, composed of trees, each of depth 1.
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

		return learn(rows, "ada", numberGenerations, poolSize, printBinaryAccuracy, ensembleSize);
	}


	private static MultiClassifier learn(List<InputRow> rows, String method, int numberGenerations,
										 int poolSize, boolean printBinaryAccuracy, int... param) {

		String description = "Model using " + rows.size() +
				" total phrases, method=" + method +
				", attributeGenerations=" + numberGenerations +
				", attributePoolSize=" + poolSize;
		if (method.equals("decision")) {
			description += ", treeDepth=" + param[0];
		} else {
			description += ", ensembleSize=" + param[0];
		}

		// each sub-problem: learning to distinguish a pair of languages
		//   - can run each of these sub-problems in parallel
		List<Decider> allTrees = getLanguagePairs().parallelStream().map(pair -> {
			Language first = pair.one;
			Language second = pair.two;

			List<InputRow> twoLanguages = rows.stream()
				.filter(i -> i.getLanguage() == first || i.getLanguage() == second)
				.collect(Collectors.toList());

			Set<Attribute> attributes =
				GeneticLearning.learnAttributes(twoLanguages, first, second, numberGenerations, poolSize);

			// learn a decision tree based on the attributes, with depth
			final Decider binaryDecider;
			if (method.equals("decision")) {
				binaryDecider = DecisionTree.learn(
					new WeightedList<>(twoLanguages), param[0], attributes, twoLanguages.size(), first, second
				);
			} else if (method.equals("ada")) {
				binaryDecider = Adaboost.learn(
					new WeightedList<>(twoLanguages), param[0], attributes, first, second
				);
			} else {
//				binaryDecider = new NeuralNetwork(
//					twoLanguages, param[0], param[1], attributes, first, second
//				);
				throw new IllegalArgumentException("Method must be 'ada' or 'decision'.");
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

		return new MultiClassifier(allTrees, description);
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
			if (confidence > 1.0) confidence = 1.0;
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

	public String getDescription() {
		return description;
	}
}
