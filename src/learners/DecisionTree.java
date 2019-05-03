package learners;

import attributes.Attributes;
import helper.Pair;
import helper.WeightedList;
import main.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DecisionTree implements Decider {

	private final Decider left; // could be a tree or a -- if false, this is evaluated
	private final Decider right; // could be a tree of b -- if true, this is evaluated
	private final Attributes splitOn; // attribute to split on

	/** Creates a decision tree given the left and right decisions, and the index of the attributes it splits on. */
	private DecisionTree(Decider left, Decider right, Attributes splitOn) {
		this.left = left;
		this.right = right;
		this.splitOn = splitOn;
	}

	/**
	 * Picks the best decision tree to represent the decision at this level, with the options left.
	 * This is a binary classifier of the two values provided.
	 * Precondition:
	 *   all data is either languageOne or languageTwo
	 * @param allData The weighted data this tree should decide on. (portion of the original data)
	 * @param levelLeft The amount of tree depth left, if this is 0, then it will be decide always the one output.
	 * @param optionsLeft The options that are left to choose from to split on.
	 * @param totalSize The total size of the original data, not just what this has to choose from.
	 * @param languageOne The first language
	 * @param languageTwo The second language
	 * @return A learned decision tree.
	 */
	public static Decider learn(WeightedList<InputRow> allData, int levelLeft, Set<Attributes> optionsLeft,
								int totalSize, String languageOne, String languageTwo) {


		// count how many have output LanguageOne
		long countOne = allData.stream().filter(i -> i.two.outputValue.equals(languageOne)).count();
		System.out.print("\nDeciding on how to split: " + countOne + " E, " + (allData.size() - countOne) + " D.");

		// base case 1: there is no levels left to iterate - make a definite decision.
		if (levelLeft == 0) {
			System.out.print("  no tree depth left, picking the majority.");
			// return the majority
			System.out.print("  entropy is: " + entropyForList(allData, languageOne) * allData.size() / totalSize);
			if (countOne > allData.size() / 2) {
				return AbsoluteDecider.fromLanguage(languageOne);
			}
			return AbsoluteDecider.fromLanguage(languageTwo);
		}

		// base case 2: there is all of one type
		if (allData.size() == countOne) { // all are the first one
			return AbsoluteDecider.fromLanguage(languageOne);
		} else if (countOne == 0) { // none are the first one, they are all the second
			return AbsoluteDecider.fromLanguage(languageTwo);
		}

		// split on the best attribute - based on information gain
		Optional<Pair<Attributes, Double>> result = optionsLeft.stream()
			.map(i -> new Pair<>(i, entropyForDecision(i, allData, totalSize, languageOne))) // pair<att, entropy>
			.min(Comparator.comparingDouble( pair -> pair.two));

		// result should be present -- if not we still have depth left but have exhausted options
		if (result.isEmpty()) {
			// ran out of options, just return the majority
			return learn(allData, 0, new HashSet<>(), totalSize, languageOne, languageTwo);
		}
		Attributes best = result.get().one;

		System.out.print("------------\nBest chosen: " + best.name());
		System.out.print("------------");

		WeightedList<InputRow> trueOnes = allData.valuesWith(best::has);
		WeightedList<InputRow> falseOnes = allData.valuesWith(best::doesntHave);
		// need copies so they don't modify each other
		Set<Attributes> optionsLeftFalse = new HashSet<>(optionsLeft);
		optionsLeftFalse.remove(best);
		Set<Attributes> optionsLeftTrue = new HashSet<>(optionsLeft);
		optionsLeftTrue.remove(best);

		// do the recursive calls
		Decider left = learn(falseOnes, levelLeft - 1, optionsLeftFalse, totalSize, languageOne, languageTwo);
		Decider right = learn(trueOnes, levelLeft - 1, optionsLeftTrue, totalSize, languageOne, languageTwo);

		// return the created tree
		return new DecisionTree(left, right, best);
	}

	/** Returns the entropy associated with a decision to split on attribute index. */
	private static double entropyForDecision(Attributes attribute, WeightedList<InputRow> allData, int totalSize, String languageOne) {
		System.out.print("  What about: " + attribute.name() + " ?");
		// split into the two sets - calculate entropy on each
		WeightedList<InputRow> falseOnes = allData.valuesWith(attribute::doesntHave);
		WeightedList<InputRow> trueOnes = allData.valuesWith(attribute::has);

		System.out.print("    x[i] = true: " + trueOnes.size());
		System.out.print("    x[i] = false: " + falseOnes.size());

		// entropy for each half * the proportion of the whole it is.
		double entropy = (double)trueOnes.size() / totalSize * entropyForList(trueOnes, languageOne) +
			(double) falseOnes.size() / totalSize * entropyForList(falseOnes, languageOne);
		System.out.print("    entropy: " + entropy + "\n");
		return entropy;
	}

	/** Returns the entropy for a list of data in bits. */
	private static double entropyForList(WeightedList<InputRow> allData, String languageOne) {
		// if there's no data, entropy is 0
		if (allData.size() == 0) return 0;

		double total = allData.totalWeight();
		// arbitrary - defined first to be true (doesn't matter since symmetric distribution).
		double eAmount = allData.valuesWith(i -> i.outputValue.equals(languageOne)).totalWeight();

		//System.out.println("A count: " + eAmount);
		//System.out.println("B count: " + (total - eAmount));
		return entropyForBoolean(eAmount / total);
	}

	/** Returns the entropy for a weighted boolean random variable. */
	private static double entropyForBoolean(double trueChance) {
		// 0 or 1 will result in NaN, but it should be 0.
		if (trueChance == 0.0 || trueChance == 1.0) {
			return 0;
		}
		// from the book −(q log2 q + (1 − q) log2(1 − q))
		double falseChance = 1 - trueChance;
		return -(trueChance * log2(trueChance) + falseChance * log2(falseChance));
	}

	/** Returns the log base two of a number. */
	static double log2(double x) {
		// log2 not defined in java in the Math class.
		return Math.log(x) / Math.log(2);
	}

	/** Instance method to make the decision on input. */
	@Override
	public String decide(InputRow row) {
		// recursive, test the inputs on the left and right side
		return splitOn.has(row) ? right.decide(row) : left.decide(row);
	}

	/** How this should be represented as a string. */
	@Override
	public String representation(int numSpaces) {
		// I'm making it like a code block.
		return "if " + splitOn.name() + " then: \n" +
			Learning.numSpaces(numSpaces + 2) + right.representation(numSpaces + 2) + "\n" +
			Learning.numSpaces(numSpaces) + "else: \n" +
			Learning.numSpaces(numSpaces + 2) + left.representation(numSpaces + 2);
	}
}
