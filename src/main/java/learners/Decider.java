package learners;

import main.InputRow;

import java.io.*;
import java.util.List;

public interface Decider extends Serializable {

	/** Returns the decision for the input value */
	LanguageDecision decide(InputRow row);

	/** Human readable output for debugging. */
	String representation(int numSpaces);

	/** Tests this decider on the input data, and returns the number incorrect / total. */
	default double errorRateUnWeighted(List<InputRow> testingData) {
		double total = testingData.size();
		double correct = testingData.stream()
			.filter(i -> this.decide(i).mostConfidentLanguage().equals(i.outputValue))
			.count();

		return (total - correct) / total;
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

	/** Loads a decider from the resources file name. */
	static Decider loadFromResources(String resourcesFileName) throws IOException, ClassNotFoundException {
		InputStream file = Decider.class.getClassLoader().getResourceAsStream(resourcesFileName);
		ObjectInputStream in = new ObjectInputStream(file);
		Decider result = (Decider) in.readObject();
		in.close();
		file.close();
		return result;
	}
}
