package attributes;

import main.InputRow;

import java.util.List;
import java.util.Random;

/***
 * Represents an attribute that is based solely on a word from the input matching this word.
 */
public class WordAttribute extends Attributes {

	/** The word that gets tested if there is a word equal to this. */
	private final String word;
	private final double fitness;

	/***
	 * Creates an attribute that tests if the input has the specified word.
	 * @param word The word to check that any of the input words match.
	 */
	public WordAttribute(String word, List<InputRow> inputs, String languageOne) {
		this.word = word;
		this.fitness = Attributes.fitness(this, inputs, languageOne);
	}

	@Override
	public boolean has(InputRow input) {
		// iterate through the words to see if one matches exactly
		for (int i = 0; i < input.words.length; i++) {
			if (input.words[i].equals(word)) return true;
		}
		return false;
	}

	@Override
	public String name() {
		return "a word is '" + word + "'";
	}

	@Override
	public double getFitness() {
		return fitness;
	}

	@Override
	public Attributes mutate(List<String> words, List<InputRow> inputs, String languageOne, String languageTwo) {
		// pick a new word from the list, since they are all there, picking a random one will
		//  more likely be a good guess
		Random r = new Random();
		int index = r.nextInt(words.size());
		return new WordAttribute(words.get(index), inputs, languageOne);
	}
}
