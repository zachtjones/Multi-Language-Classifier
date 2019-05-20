package attributes;

import learners.NetNode;
import main.InputRow;

import java.io.Serializable;
import java.util.List;

public abstract class Attributes implements Serializable, Comparable<Attributes>, NetNode {

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
	 * Precondition: all the elements are one of the two languages.
	 * @param inputs The list of inputs that are used to test the attribute
	 * @param languageOne The first language to test
	 * @return The fitness calculation.
	 */
	static double fitness(Attributes thing, List<InputRow> inputs, String languageOne) {

		// could see the best decision tree that could be learned from this attribute,
		//   but it's faster to do this way since there's a lot of simplification that can be done:
		// One: the inputs are always unweighted
		// Two: the tree is only 1 deep
		//   - don't have to iterate through all 3x, and create the two sub-lists to handle each definite decision

		int countHasLangOne = 0;
		int countHasLangTwo = 0;
		int countNoLangOne = 0;
		int countNoLangTwo = 0;

		for (InputRow r : inputs) {
			if (thing.has(r)) {
				if (r.outputValue.equals(languageOne)) countHasLangOne++;
				else countHasLangTwo++;
			} else {
				if (r.outputValue.equals(languageOne)) countNoLangOne++;
				else countNoLangTwo++;
			}
		}

		// for each part, pick the larger side -- what the decision tree would do
		double correctHas = Math.max(countHasLangOne, countHasLangTwo);
		double correctNo = Math.max(countNoLangOne, countNoLangTwo);

		return (correctHas + correctNo) / (countHasLangOne + countHasLangTwo + countNoLangOne + countNoLangTwo);

	}

	/** returns the fitness cached */
	public abstract double getFitness();

	/**
	 * Returning another attribute that is based on this, but slightly different.
	 * @param words A list of words to use for the mutation to be a representative sample.
	 * @return The new attribute, which is a mutation of this.
	 */
	public abstract Attributes mutate(List<String> words, List<InputRow> inputs, String languageOne, String languageTwo);

	/** Method to help with the neural network, attributes can be treated as Nodes in the network. */
	@Override
	public double activation(InputRow row) {
		return has(row) ? 1.0 : -1.0;
	}
}
