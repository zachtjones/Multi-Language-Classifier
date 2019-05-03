package attributes;

import helper.WeightedList;
import learners.DecisionTree;
import learners.Decider;
import main.InputRow;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Attributes implements Serializable, Comparable<Attributes> {

	@Override
	public int compareTo(Attributes other) {
		// sort by the fitness, then name to split ties (don't want to attributes that are equivalent)
		int result = Double.compare(this.getFitness(), other.getFitness());
		if (result == 0) {
			return this.name().compareTo(other.name());
		} else {
			return result;
		}
	}

	/** Method to determine if the attribute is true on this input row. */
	public abstract boolean has(InputRow input);

	/** A human-readable name for this attribute. */
	public abstract String name();

	/** Returns the name. This way objects are required to implement it */
	public final String toString() {
		return name();
	}

	/** Helper method to make code elsewhere more concise. */
	public boolean doesntHave(InputRow input) {
		return !has(input);
	}

	/***
	 * Calculates the fitness of this attribute on a scale of 0 to 1.0, based on the percent.
	 * @param inputs The list of inputs that are used to test the attribute
	 * @param languageOne The first language to test
	 * @param languageTwo The second language
	 * @return The fitness calculation.
	 */
	public static double fitness(Attributes thing, List<InputRow> inputs, String languageOne, String languageTwo) {
		Set<Attributes> justThis = new HashSet<>();
		justThis.add(thing);

		Decider tree = DecisionTree.learn(
			new WeightedList<>(inputs), 1, justThis, inputs.size(), languageOne, languageTwo);

		// test the tree error weight
		return 1 - tree.errorRateUnWeighted(inputs);
	}

	/** returns the fitness cached */
	public abstract double getFitness();

	/**
	 * Returning another attribute that is based on this, but slightly different.
	 * @param words A list of words to use for the mutation to be a representative sample.
	 * @return The new attribute, which is a mutation of this.
	 */
	public abstract Attributes mutate(List<String> words, List<InputRow> inputs, String languageOne, String languageTwo);
}
