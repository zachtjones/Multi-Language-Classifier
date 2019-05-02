package main;

import helper.WeightedList;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Attributes implements Serializable {

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
	public double fitness(List<InputRow> inputs, String languageOne, String languageTwo) {
		int count = 0;
		Set<Attributes> justThis = new HashSet<>();
		justThis.add(this);

		Decider tree = DecisionTree.learn(
			new WeightedList<>(inputs), 1, justThis, inputs.size(), languageOne, languageTwo);

		// test the tree error weight
		return 1 - tree.errorRateUnWeighted(inputs);
	}

	/**
	 * Returning another attribute that is based on this, but slightly different.
	 * @param words A list of words to use for the mutation to be a representative sample.
	 * @return The new attribute, which is a mutation of this.
	 */
	public abstract Attributes mutate(List<String> words);
}
