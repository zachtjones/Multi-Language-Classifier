package learners;

import attributes.Attributes;
import helper.Pair;
import helper.WeightedList;
import helper.MultiLanguageDecision;
import com.zachjones.languageclassifier.entities.InputRow;

import java.util.Set;
import java.util.stream.Collectors;

public class Adaboost implements Decider {

	/** The languages to split between. */
	private final String languageOne, languageTwo;

	/** weighted inputs and hypotheses used in learning.
	 * These weights change as we go through. */
	private WeightedList<InputRow> inputs;

	/** This one is the weights according to the correct % of each hypothesis.
	 * We need to keep this not normalized and then do the normalization at the end of each iteration. */
	private WeightedList<Decider> hypotheses;

	/** This list will always be a normalized list of the individual stumps. */
	private WeightedList<Decider> normalizedHypotheses;

	/** Private constructor to create an Adaboost ensemble */
	private Adaboost(WeightedList<InputRow> input, String languageOne, String languageTwo) {
		hypotheses = new WeightedList<>();
		this.inputs = input;
		this.languageOne = languageOne;
		this.languageTwo = languageTwo;
	}

	/** Learns using the Adaboost technique on decision stumps.
	 * This learns a binary classifier between the two languages.
	 * @param inputs The inputs to decide on. Should initially be unweighted.
	 * @param ensembleSize The number of stumps to create.
	 * @param attributes The set of potential attributes to use.
	 * @param languageOne The first language of the inputs
	 * @param languageTwo The second language of the inputs
	 */
	static Decider learn(WeightedList<InputRow> inputs, int ensembleSize, Set<Attributes> attributes,
						 String languageOne, String languageTwo) {

		Adaboost result = new Adaboost(inputs, languageOne, languageTwo);
		int totalSize = inputs.size();

		for (int i = 0; i < ensembleSize; i++) {
			Decider newHypothesis =
				DecisionTree.learn(result.inputs, 1, attributes, totalSize, languageOne, languageTwo);

			double error = 0.0;
			for (int j = 0; j < inputs.size(); j++) {
				Pair<Double, InputRow> row = inputs.get(j); // weight, example
				if (!newHypothesis.decide(row.two).mostConfidentLanguage().equals(row.two.getOutputValue())) {
					error += row.one;
				}
			}

			// special case: we learn it 100%, should just add it with normal weight
			if (error == 0.0) {
				result.hypotheses.addWeight(1.0, newHypothesis);
				result.normalizedHypotheses = result.hypotheses.normalize();
				break;
			}

			// reduce the weights of the ones we got right
			double reductionRate = error / (1 - error);
			for (int j = 0; j < result.inputs.size(); j++) {
				Pair<Double, InputRow> row = result.inputs.get(j);
				if (newHypothesis.decide(row.two).mostConfidentLanguage().equals(row.two.getOutputValue())) {
					result.inputs.setWeight(j, result.inputs.getWeight(j) * reductionRate);
				}
			}

			// normalize weights
			result.inputs = result.inputs.normalize();

			// calculate the weight for this hypothesis and add it
			result.hypotheses.addWeight(DecisionTree.log2((1 - error) / error), newHypothesis);
			result.normalizedHypotheses = result.hypotheses.normalize();
		}
		return result;
	}

	/** Represents the ensemble's decision on the input row. */
	@Override
	public LanguageDecision decide(InputRow row) {

		// map the first language to -1, and the second language to 1
		MultiLanguageDecision weights = new MultiLanguageDecision();

		for (int i = 0; i < normalizedHypotheses.size(); i++) {

			// iterate through the hypotheses, weighting their predictions
			double weight = normalizedHypotheses.getWeight(i);
			weights.addWeightTo(normalizedHypotheses.getItem(i).decide(row), weight);
		}

		weights.normalize();

		return weights;
	}

	@Override
	public String representation(int numSpaces) {
		String rep = "Adaboost on the following " + this.hypotheses.size() + " decision stumps:\n";

		String each = normalizedHypotheses.stream()
			.map(i -> "Weight: " + i.one + ", stump:\n" + i.two.representation(0))
			.collect(Collectors.joining("\n\n"));

		return rep + "\n" + each;
	}
}
