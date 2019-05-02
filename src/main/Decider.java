package main;

import helper.WeightedList;

import java.io.*;
import java.util.List;

public interface Decider extends Serializable {

	/** Returns the decision for the input value */
	String decide(InputRow row);

	/** Human readable output for debugging. */
	String representation(int numSpaces);

	/** Tests this decider on the input data, and returns the number incorrect / total. */
	default double errorRateUnWeighted(List<InputRow> testingData) {
		double total = testingData.size();
		double correct = testingData.stream().filter(i -> this.decide(i).equals(i.outputValue)).count();
		return (total - correct) / total;
	}

	/** Tests this decider on the input data, with weights.
	 * @param testingData The data to test on. Should be normalized. */
	default double errorRateWeighted(WeightedList<InputRow> testingData) {
		double totalWeight = 1.0;
		// map to <expected, actual>, count the wrong ones.
		double weightWrong = testingData.stream()
			.filter(i -> !this.decide(i.two).equals(i.two.outputValue)) // keep wrong ones only
			.mapToDouble(i -> i.one) // extract the weight
			.sum(); // total of the weights of wrong ones

		return weightWrong / totalWeight;
	}

	/** Writes this object using java serialization to the filename. */
	default void saveTo(String fileName) throws IOException {
		FileOutputStream file = new FileOutputStream(fileName);
		ObjectOutputStream out = new ObjectOutputStream(file);
		out.writeObject(this);
		out.flush();
		out.close();
		file.close();
	}

	/** Loads a decider from the file using java deserialization */
	static Decider loadFromFile(String fileName) throws IOException, ClassNotFoundException {
		FileInputStream file = new FileInputStream(fileName);
		ObjectInputStream in = new ObjectInputStream(file);
		Decider result = (Decider) in.readObject();
		in.close();
		file.close();
		return result;
	}
}
