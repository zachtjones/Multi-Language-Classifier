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
	private final String languageOne;
	private final String languageTwo;
	private final int numberAttributes;

	/** Nodes in this network [layer number][numberInLayer] */
	private final Perceptron[][] nodes;
	private final Perceptron finalPerceptron; // the one in the output layer


	public NeuralNetwork(List<InputRow> rows, int hiddenLayers, int nodesPerLayer,
										Set<Attributes> attributes, String languageOne, String languageTwo) {
		this.rows = rows;
		this.hiddenLayers = hiddenLayers;
		this.nodesPerLayer = nodesPerLayer;
		this.attributes = new ArrayList<>(attributes); // need an order since we have arrays
		this.numberAttributes = attributes.size();

		this.languageOne = languageOne;
		this.languageTwo = languageTwo;

		nodes = new Perceptron[hiddenLayers + 1][nodesPerLayer];

		// all nodes[0] take in the converted inputs
		for (int k = 0; k < nodesPerLayer; k++) {
			nodes[0][k] = new Perceptron(numberAttributes);
		}
		// the rest have the number of inputs as the nodesPerLayer
		for (int i = 0; i < hiddenLayers; i++) {
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
		// languageOne -> 1, languageTwo -> -1
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
		StringBuilder result = new StringBuilder("A neural network composed of nodes:\n\n");

		for (int i = 0; i < nodes.length; i++) {
			result.append("Layer: ");
			result.append(i + 1);
			result.append('\n');

			for (int j = 0; j < nodes[i].length; j++) {
				result.append("  Node: ");
				result.append(j);
				result.append(" has weights: ");
				result.append(Arrays.toString(nodes[i][j].weights));
				result.append('\n');
			}
		}

		return result.toString();
	}
}
