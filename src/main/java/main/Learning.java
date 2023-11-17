package main;

import com.zachjones.languageclassifier.entities.InputRow;
import examples.GetWikipediaContent;
import helper.Pair;
import learners.Decider;
import learners.MultiClassifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
	private static boolean isDownloadingExamples, isLearning, isTesting, isEvaluating = false;

	// examples
	private static int numberExamples = 200;
	private static String examplesFile;
	private static String testingFile;

	// learning
	private static String method = "decisionTree"; // decisionTree, AdaBoost, or neural
	private static String learnerFile;
	private static int numberGenerations = 50;
	private static int poolSize = 12;
	private static int treeDepth = 6;
	private static int ensembleSize = 6; // learners in the AdaBoost ensemble
	private static int hiddenLayers = 1; // neural network hidden layer count
	private static int nodesPerLayer = 10; // number of nodes per neural network layer

	// extra printing
	private static boolean printBinaryAccuracy = false;


	private static void parseArguments(String[] args) {
		for (String i : args) {
			// keywords
			if (i.equals("examples")) isDownloadingExamples = true;
			if (i.equals("learn")) isLearning = true;
			if (i.equals("test")) isTesting = true;
			if (i.equals("evaluate")) isEvaluating = true;
			if (i.equalsIgnoreCase("decisionTree")) method = "decisionTree";
			if (i.equalsIgnoreCase("adaboost")) method = "adaboost";
			if (i.equalsIgnoreCase("neural")) method = "neural";

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
			if (i.startsWith("ensembleSize"))
				ensembleSize = Integer.parseInt(i.replace("ensembleSize=", ""));
			if (i.startsWith("hiddenLayers="))
				hiddenLayers = Integer.parseInt(i.replace("hiddenLayers=", ""));
			if (i.startsWith("nodesPerLayer="))
				nodesPerLayer = Integer.parseInt(i.replace("nodesPerLayer=", ""));

			// printing stuff
			if (i.equals("printBinaryAccuracy")) printBinaryAccuracy = true;
		}

		if (!isDownloadingExamples && !isLearning && !isTesting && !isEvaluating) {
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
		// learn adaboost examplesFile=data/training-1.txt learner=data/learner-1.dat
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

			System.out.printf("Attributes learning using: numberGenerations=%d, poolSize=%d, examplesFile=%s%n",
				numberGenerations, poolSize, examplesFile);

			// read the inputs
			List<InputRow> rows = InputRow.Companion.loadExamples(examplesFile);
			final MultiClassifier m;

			if (method.equals("decisionTree")) {

				System.out.printf(
					"Learning decisionTree, treeDepth=%d, learnerFile=%s%n",
					treeDepth, learnerFile);

				// learn
				m = MultiClassifier.learnDecisionTree(
					rows, treeDepth, numberGenerations, poolSize, printBinaryAccuracy
				);

			} else if (method.equals("adaboost")) { // adaboost learning algorithm, tree depth 1

				System.out.printf(
					"Learning AdaBoost on decision tree stumps, ensembleSize=%d, treeDepth=1, learnerFile=%s%n",
					ensembleSize, learnerFile);

				// learn
				m = MultiClassifier.learnAdaBoost(
					rows, ensembleSize, numberGenerations, poolSize, printBinaryAccuracy
				);

			} else {

				System.out.printf("Learning using Neural Networks, hiddenLayers=%d, nodesPerLayer=%d%n",
					hiddenLayers, nodesPerLayer);

				// learn
				m = MultiClassifier.learnNeuralNetwork(
					rows, hiddenLayers, nodesPerLayer, numberGenerations, poolSize, printBinaryAccuracy
				);
			}

			//System.out.println(m.representation(0));


			// evaluate learner
			double accuracyPercent = 100 * (1 - m.errorRateUnWeighted(rows));
			System.out.println("Training accuracy: " + accuracyPercent);

			// save to file
			m.saveTo(learnerFile);
		}

		// test a learner
		if (isTesting) {
			// requires testing data (labeled)
			// requires learner file
			if (testingFile == null) usage();
			if (learnerFile == null) usage();

			Decider learner = Decider.loadFromFile(learnerFile);
			double result = 100 * (1 - learner.errorRateUnWeighted(InputRow.Companion.loadExamples(testingFile)));
			System.out.println("Testing accuracy: " + result);
		}

		// evaluate a learner (given new inputs, print out the evaluation)
		if (isEvaluating) {
			// requires learner file
			if (learnerFile == null) usage();

			System.out.printf("Using learner:%s%n", learnerFile);
			System.out.println("Enter a phrase, followed by a new line.");

			Decider learner = Decider.loadFromFile(learnerFile);

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while ((line = br.readLine()) != null) {
				InputRow row = new InputRow(line);
				System.out.println(learner.decide(row));
			}
		}

		System.out.println("\nRunning time (s) " + (System.currentTimeMillis() - startTime) / 1_000.0);
	}

	private static void usage() {
		System.out.println("Usage: (argument position doesn't matter), optionals are in parentheses\n" +
			"\texamples examplesFile=  testingFile=  (numberExamples=200)\n" +
			"\tlearn (decisionTree | adaboost | neural) examplesFile=   (numberGenerations=50)  (poolSize=12) " +
			" (treeDepth=6)  (ensembleSize=6)  (hiddenLayers=1)  (nodesPerLayer=10) " +
			"learnerFile=  (printBinaryAccuracy)\n" +
			"\ttest testingFile=  learnerFile=\n" +
			"\tevaluate learnerFile=");
		System.exit(0);
	}

	/** Creates a string of length numSpaces comprised of spaces. */
	public static String numSpaces(int numSpaces) {
		return new String(new char[numSpaces]).replace('\0', ' ');
	}
}
