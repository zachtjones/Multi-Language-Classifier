package main;

import examples.GetWikipediaContent;
import helper.Pair;
import learners.Decider;
import learners.MultiClassifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Learning {

	// arguments to the parameters

	// tasks
	private static boolean isDownloadingExamples, isLearning, isTesting = false;

	// examples
	private static int numberExamples = 200;
	private static String examplesFile;
	private static String testingFile;

	// learning
	private static String method = "decisionTree"; // just decisionTree so far
	private static String learnerFile;
	private static int numberGenerations = 50;
	private static int poolSize = 12;
	private static int treeDepth = 6;

	// extra printing
	private static boolean printBinaryAccuracy = false;


	private static void parseArguments(String[] args) {
		for (String i : args) {
			// keywords
			if (i.equals("examples")) isDownloadingExamples = true;
			if (i.equals("learn")) isLearning = true;
			if (i.equals("test")) isTesting = true;
			if (i.equals("decisionTree")) method = "decisionTree";

			// files
			if (i.startsWith("examplesFile="))
				examplesFile = i.replace("examplesFile=", "");
			if (i.startsWith("testingFile="))
				testingFile = i.replace("testingFile=", "");
			if (i.startsWith("learnerFile="))
				learnerFile = i.replace("learnerFile=", "");

			// arguments to methods
			if (i.startsWith("numberExamples="))
				numberExamples = Integer.parseInt(i.replace("numberExamples=", ""));
			if (i.startsWith("numberGenerations="))
				numberGenerations = Integer.parseInt(i.replace("numberGenerations=", ""));
			if (i.startsWith("poolSize="))
				poolSize = Integer.parseInt(i.replace("poolSize=", ""));
			if (i.startsWith("treeDepth"))
				treeDepth = Integer.parseInt(i.replace("treeDepth=", ""));

			// printing stuff
			if (i.equals("printBinaryAccuracy")) printBinaryAccuracy = true;
		}

		if (!isDownloadingExamples && !isLearning && !isTesting) {
			usage();
		}
	}

	/**
	 * Returns a list of pairs of languages and their corresponding URL to use for wikipedia content.
	 * @return The list of Pair&lt;String, URL&gt;
	 */
	public static List<Pair<String, URL>> getLanguageUrls() {
		try {
			return Arrays.asList(
				new Pair<>("Albanian", new URL("https://sq.wikipedia.org/wiki/Speciale:Rast%C3%ABsishme")),
				new Pair<>("Croatia", new URL("https://hr.wikipedia.org/wiki/Posebno:Slu%C4%8Dajna_stranica")),
				new Pair<>("Czech", new URL("https://cs.wikipedia.org/wiki/Speci%C3%A1ln%C3%AD:N%C3%A1hodn%C3%A1_str%C3%A1nka")),
				new Pair<>("Danish", new URL("https://da.wikipedia.org/wiki/Speciel:Tilf%C3%A6ldig_side")),
				new Pair<>("Dutch", new URL("https://nl.wikipedia.org/wiki/Speciaal:Willekeurig")),
				new Pair<>("English", new URL("https://en.wikipedia.org/wiki/Special:Random")),
				new Pair<>("French", new URL("https://fr.wikipedia.org/wiki/Sp%C3%A9cial:Page_au_hasard")),
				new Pair<>("Gaelic", new URL("https://gd.wikipedia.org/wiki/S%C3%B2nraichte:Random")),
				new Pair<>("German", new URL("https://de.wikipedia.org/wiki/Spezial:Zuf%C3%A4llige_Seite")),
				new Pair<>("Hawaiian", new URL("https://haw.wikipedia.org/wiki/Papa_nui:Kaulele")),
				new Pair<>("Icelandic", new URL("https://is.wikipedia.org/wiki/Kerfiss%C3%AD%C3%B0a:Handah%C3%B3fsvalin_s%C3%AD%C3%B0a")),
				new Pair<>("Italian", new URL("https://it.wikipedia.org/wiki/Speciale:PaginaCasuale")),
				new Pair<>("Romanian", new URL("https://ro.wikipedia.org/wiki/Special:Aleatoriu")),
				new Pair<>("Samoan", new URL("https://sm.wikipedia.org/wiki/Special:Random")),
				new Pair<>("Spanish", new URL("https://es.wikipedia.org/wiki/Especial:Aleatoria"))
			);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	/** Holds a list of all the languages. */
	public final static List<String> languages = getLanguageUrls().stream().map(i -> i.one).collect(Collectors.toList());

	/***
	 * Holds all the pairs of languages that could be used, but in alphabetical order for the languages.
	 */
	public final static List<Pair<String, String>> languagePairs;
	static {
		languagePairs = new ArrayList<>();
		// obtain pairwise attributes that are good to split by
		for (int i = 0; i < languages.size(); i++) {
			String firstLanguage = languages.get(i);
			for (int j = i + 1; j < languages.size(); j++) {
				String secondLanguage = languages.get(j);
				languagePairs.add(new Pair<>(firstLanguage, secondLanguage));
			}
		}
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		parseArguments(args);

		long startTime = System.currentTimeMillis();

		// first download examples, if needed
		if (isDownloadingExamples) {
			if (examplesFile != null) {
				System.out.println("Downloading examples into: " + examplesFile);
				GetWikipediaContent.main(examplesFile, numberExamples);
			}
			if (testingFile != null) {
				System.out.println("Downloading examples into: " + testingFile);
				GetWikipediaContent.main(testingFile, numberExamples);
			}
		}

		// then learn from the downloaded files unless they're already there
		if (isLearning) {
			// requires example file
			if (examplesFile == null) usage();
			if (learnerFile == null) usage();

			System.out.printf(
				"Learning %s, examplesFile=%s, numberGenerations=%d, poolSize=%d, treeDepth=%d, learnerFile=%s%n",
				method, examplesFile, numberGenerations, poolSize, treeDepth, learnerFile);

			if (method.equals("decisionTree")) {
				// read the inputs
				List<InputRow> rows = InputRow.loadExamples(examplesFile);

				// learn
				MultiClassifier m = MultiClassifier.learnDecisionTree(
					rows, treeDepth, numberGenerations, poolSize, printBinaryAccuracy
				);

				//System.out.println(m.representation(0));

				// evaluate learner
				double accuracyPercent = 100 * (1 - m.errorRateUnWeighted(rows));
				System.out.println("Training accuracy: " + accuracyPercent);

				// save to file
				m.saveTo(learnerFile);
			} else {
				usage();
			}
		}

		// evaluate a learner
		if (isTesting) {
			// requires testing data (labeled)
			// requires learner file
			if (testingFile == null) usage();
			if (learnerFile == null) usage();

			Decider learner = Decider.loadFromFile(learnerFile);
			double result = 100 * (1 - learner.errorRateUnWeighted(InputRow.loadExamples(testingFile)));
			System.out.println("Testing accuracy: " + result);
		}

		System.out.println("\nRunning time (s) " + (System.currentTimeMillis() - startTime) / 1_000.0);
	}

	private static void usage() {
		System.out.println("Usage: (argument position doesn't matter), optionals are in parentheses\n" +
			"\texamples examplesFile=  testingFile=  (numberExamples=200)\n" +
			"\tlearn (decisionTree) examplesFile=   (numberGenerations=50)  (poolSize=12)  (treeDepth=6)  " +
			"learnerFile=  (printBinaryAccuracy)\n" +
			"\ttest testingFile=  learnerFile=");
		System.exit(0);
	}

	/** Creates a string of length numSpaces comprised of spaces. */
	public static String numSpaces(int numSpaces) {
		return new String(new char[numSpaces]).replace('\0', ' ');
	}
}
