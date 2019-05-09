package learners;

import attributes.Attributes;
import attributes.LetterFrequencyAttribute;
import attributes.WordAttribute;
import attributes.WordEndingAttribute;
import main.InputRow;

import java.util.*;

public class GeneticLearning {

	/** Holds the pool of potential attributes */
	private final TreeSet<Attributes> pool;

	/** Holds a list of all the words that are in the input. */
	private final List<String> allWords;

	/** Holds the input values list */
	private final List<InputRow> inputs;

	/** The two languages to split between */
	private final String languageOne, languageTwo;

	/** Random used for chances with mutation possibilities */
	private final Random r = new Random();

	/**
	 * Creates the learning pool with a genetic algorithm.
	 * @param inputs The input rows to use as part of the measure for fitness
	 * @param languageOne The first language.
	 * @param languageTwo The second language.
	 */
	private GeneticLearning(List<InputRow> inputs, String languageOne, String languageTwo) {
		this.languageOne = languageOne;
		this.languageTwo = languageTwo;

		this.inputs = inputs;
		// pool is sorted by the fitness of the feature
		pool = new TreeSet<>();
		allWords = new ArrayList<>(inputs.size() * 20);
		for (InputRow row : inputs) {
			allWords.addAll(Arrays.asList(row.words));
		}

		// fill in the pool with some randomly drawn attributes
		Attributes noUse = new WordAttribute("a", inputs, languageOne);
		for (int i = 0; i < 20; i++) {
			pool.add(noUse.mutate(allWords, inputs, languageOne, languageTwo));
		}

		// letter frequency
		Attributes noUseLetters = new LetterFrequencyAttribute('a', 'z', inputs, languageOne);
		for (int i = 0; i < 20; i++) {
			pool.add(noUseLetters.mutate(allWords, inputs, languageOne, languageTwo));
		}

		// word endings
		Attributes noUseEnding = new WordEndingAttribute("", inputs, languageOne);
		for (int i = 0; i < 20; i++) {
			pool.add(noUseEnding.mutate(allWords, inputs, languageOne, languageTwo));
		}

		// TODO similar process for other attribute types once they are added

	}

	/**
	 * Proceeds to the next generation, performing mutation, crossover, and then trimming
	 * the results down to the pool size, keeping the most fit.
	 */
	private void nextGeneration(int maxPoolSize) {
		// mutate some random ones, proportional to their fitness
		TreeSet<Attributes> newOnes = new TreeSet<>();

		for (Attributes i : pool) {
			double chance = i.getFitness();
			if (r.nextDouble() < chance) {
				// do the mutation and add it to the pool
				newOnes.add(i.mutate(allWords, inputs, languageOne, languageTwo));
			}
		}

		// TODO add the crossover once that's implemented

		// add the new ones to the list
		pool.addAll(newOnes);

		// trim the pool to MAX_POOL_SIZE (aka natural selection in the genetic sense)
		while (pool.size() > maxPoolSize) {
			pool.pollFirst();
		}
	}

	/***
	 * Learns a set of attributes from the input data, given a genetic algorithm.
	 * Precondition: all examples are either language one or two.
	 * @param inputs The labeled data to learn from (contains all examples, not just languageOne and two).
	 * @param languageOne The first language.
	 * @param languageTwo The second language.
	 * @param numberGenerations The number of generations to run
	 * @param poolSize The pool size of attributes to retain.
	 * @return The set of attributes learned.
	 */
	static Set<Attributes> learnAttributes(List<InputRow> inputs, String languageOne, String languageTwo,
											int numberGenerations, int poolSize) {
		// iterate 100 generations
		GeneticLearning learning = new GeneticLearning(inputs, languageOne, languageTwo);
		for (int i = 0; i < numberGenerations; i++) {
			learning.nextGeneration(poolSize);
		}

		return learning.pool;
	}
}
