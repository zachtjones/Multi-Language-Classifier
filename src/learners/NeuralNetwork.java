package learners;

import attributes.Attributes;
import main.InputRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/***
 * Represents a neural network of perceptrons, used to classify between a language pair.
 */
public class NeuralNetwork implements Decider {

	private final List<InputRow> rows;
	private final int hiddenLayers;
	private final int nodesPerLayer;
	private final List<Attributes> attributes;
	private final int numberAttributes;

	/** The first language, this get mapped to 1.0 */
	private final String languageOne;
	/** The second language, this get mapped to -1.0 */
	private final String languageTwo;


	/** Nodes in this network [layer number][numberInLayer]
	 * layerNumber = 0 ones that depend on input, */
	private final Perceptron[][] nodes;
	private final Perceptron finalPerceptron; // the one in the output layer

	private final static double learningRate = 0.05;


	public NeuralNetwork(List<InputRow> rows, int hiddenLayers, int nodesPerLayer,
										Set<Attributes> attributes, String languageOne, String languageTwo) {
		this.rows = rows;
		this.hiddenLayers = hiddenLayers;
		this.nodesPerLayer = nodesPerLayer;
		this.attributes = new ArrayList<>(attributes); // need an order since we have arrays
		this.numberAttributes = attributes.size();

		this.languageOne = languageOne;
		this.languageTwo = languageTwo;

		nodes = new Perceptron[hiddenLayers][nodesPerLayer];

		// all nodes[0] take in the converted inputs
		// first (hidden) layer takes the number of attributes inputs
		if (hiddenLayers > 0) {
			for (int k = 0; k < nodesPerLayer; k++) {
				nodes[0][k] = new Perceptron(numberAttributes);
			}
		}

		// the rest have the number of inputs as the nodesPerLayer
		for (int i = 0; i < hiddenLayers - 1; i++) {
			for (int j = 0; j < nodesPerLayer; j++) {
				nodes[i + 1][j] = new Perceptron(nodesPerLayer);
			}
		}

		// our final perceptron, combining the results of the previous layer
		if (hiddenLayers > 0) {
			finalPerceptron = new Perceptron(nodesPerLayer);
		} else {
			finalPerceptron = new Perceptron(numberAttributes);
		}

		// TODO do a loop to continue adjusting weights until they converge
		// they don't always converge
		for (int k = 0; k < 30; k++) {
			// update the final perceptron if incorrect
			double totalUpdates = 0.0;
			for (InputRow row : rows) {
				String decision = this.decide(row).mostConfidentLanguage();
				if (!decision.equals(row.outputValue)) {
					// update the perceptron, don't update the intercept
					for (int i = 1; i < finalPerceptron.weights.length; i++) {
						// determine the input to finalPerceptron
						double input = this.attributes.get(i).has(row) ? 1.0 : -1.0;
						double expectedOutput = row.outputValue.equals(languageOne) ? 1.0 : -1.0;
						double update = learningRate * input * expectedOutput;
						totalUpdates += Math.abs(update);
						finalPerceptron.weights[i] += update;
					}
				}
			}
			// there's not any changes from the previous iteration, stop.
			if (totalUpdates == 0) {
				break;
			}
		}
		System.out.println();

	}

	@Override
	public LanguageDecision decide(InputRow row) {
		// true -> 1.0;
		// false -> -1.0;
		// calculate the values as they go through the network
		double[] inputsConverted = new double[numberAttributes];
		for (int k = 0; k < numberAttributes; k++) {
			inputsConverted[k] = attributes.get(k).has(row) ? 1.0 : -1.0;
		}

		// need a working array of doubles for the calculations through
		double[] inputs = inputsConverted;
		double[] nextIteration;

		// work through the network, working left to right, based on the calculations of previous layer
		for (Perceptron[] currentLayer : nodes) {

			nextIteration = new double[nodesPerLayer];

			for (int j = 0; j < nodesPerLayer; j++) {
				nextIteration[j] = currentLayer[j].calculateResult(inputs);
			}

			// outputs of this layer feed the inputs of the next one.
			inputs = nextIteration;
		}

		double result = finalPerceptron.calculateResult(inputs); // range [-1.0, 1.0]
		double probabilityLanguageOne = (result + 1) / 2; // range [0, 1.0]
		return new ConfidenceDecider(languageOne, languageTwo, probabilityLanguageOne);
	}

	@Override
	public String representation(int numSpaces) {
		StringBuilder result = new StringBuilder("A neural network: ");
		result.append(languageOne);
		result.append("(value 1) vs ");
		result.append(languageTwo);
		result.append("(value -1)\n");

		result.append("  Attributes order: ");
		result.append(attributes.toString());
		result.append('\n');

		for (int i = 0; i < nodes.length; i++) {
			result.append("Layer: ");
			result.append(i + 1);
			result.append('\n');

			for (int j = 0; j < nodes[i].length; j++) {
				result.append("  Node: ");
				result.append(j);
				result.append(" bias: ");
				result.append(nodes[i][j].bias);
				result.append(" has weights: ");
				result.append(Arrays.toString(nodes[i][j].weights));
				result.append('\n');
			}
		}

		result.append("Output node has weights: ");
		result.append(Arrays.toString(finalPerceptron.weights));
		result.append('\n');

		return result.toString();
	}
}
