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
			double result = updateWeights();
			// stop if the weights don't change
			if (result == 0.0)
				break;
		}

	}

	/** Updates the weights, returning the total amount of weights changed */
	private double updateWeights() {
		// update the final perceptron if incorrect
		double totalUpdates = 0.0;
		for (InputRow row : rows) {
			String decision = this.decide(row).mostConfidentLanguage();
			if (!decision.equals(row.outputValue)) {
				totalUpdates = updateWeightsForInput(totalUpdates, row);
			}
		}

		return totalUpdates;
	}

	/** Updates the weights for a specific example.
	 * This is only called for examples that are classified incorrectly */
	private double updateWeightsForInput(double totalUpdates, InputRow row) {

		// it's already determined that this classification is incorrect, relay the error back
		//  throughout the network

		// have to iterate backwards so we don't calculate things twice
		// responsibility for these goes back from the final node

		// first step: calculate all the activations:
		// need a working array of doubles for the calculations through, initially the inputs
		double[][] activations = new double[hiddenLayers][];
		double[] inputActivations = calculateInputValues(row);

		// working values
		double[] inputs = inputActivations;
		double[] nextIteration;

		// work through the network, working left to right, based on the calculations of previous layer
		for (int i = 0; i < nodes.length; i++) {
			Perceptron[] currentLayer = nodes[i];

			nextIteration = new double[nodesPerLayer];

			for (int j = 0; j < nodesPerLayer; j++) {
				double activation = currentLayer[j].calculateResult(inputs);
				activations[i][j] = activation;
				nextIteration[j] = activation;
			}

			// outputs of this layer feed the inputs of the next one.
			inputs = nextIteration;
		}
		final double activationOutput = finalPerceptron.calculateResult(inputs);

		// second step: calculate all the delta's:
		//  sum over each weight: alpha * (y - predicted)*weight * (1 - predicted)*previous activation

		double deltaOutputNode = 0;
		double expected = mapOutputToDouble(row.outputValue);
		// sum over the nodes in the last activations
		if (hiddenLayers > 0) {
			// base on last nodes in hidden layer
			double[] lastActivations = activations[hiddenLayers - 1];

			for (int i = 0; i < nodesPerLayer; i++) {
				double act = lastActivations[i];
				double err = learningRate * (expected - activationOutput) * finalPerceptron.weights[i];
				double slope = (1 - finalPerceptron.weights[i]) * act;
				deltaOutputNode += err * slope;
			}

		} else {
			// base on inputs
			for (int i = 0; i < inputActivations.length; i++) {
				double act = inputActivations[i];
				double err = learningRate * (expected - activationOutput) * finalPerceptron.weights[i];
				double slope = (1 - finalPerceptron.weights[i]) * act;
				deltaOutputNode += err * slope;
			}

		}

		double[][] deltas = new double[hiddenLayers][nodesPerLayer];
		// calculate back the delta's on the previous ones
		for (int i = hiddenLayers - 1; i >= 0; i--) {
			double[] lastActivations = i == 0 ? inputActivations : activations[i - 1];
			final Perceptron[] goingToPerceptrons;
			if (i == hiddenLayers - 1) {
				goingToPerceptrons = new Perceptron[]{finalPerceptron};
			} else {
				goingToPerceptrons = nodes[i + 1];
			}

			for (int j = 0; j < nodesPerLayer; j++) {
				// calculate the delta for node[i][j] = activation[i][j] * sum of each:
				//   delta's that this goes to * the weight that goes to it
				for (Perceptron goingTo : goingToPerceptrons) {
					final double deltaGoingTo;
					if (i == hiddenLayers - 1) {
						deltaGoingTo = deltaOutputNode;
					} else {
						deltaGoingTo = deltas[i + 1][j];
					}
					deltas[i][j] += deltaGoingTo * goingTo.weights[j];
				}
				double slope = (1 - lastActivations[j]);
				deltas[i][j] *= slope;
			}
		}

		// have calculated all delta's, now we can update all the weights
		for (int i = 0; i < hiddenLayers; i++) {
			for (int j = 0; j < nodesPerLayer; j++) {
				// for k up to nodesPerLayer
				// node[i][j] weight to node[i + 1][k] is incremented by that activation * delta
				
			}
		}

		// update the perceptron, don't update the intercept
		for (int i = 1; i < finalPerceptron.weights.length; i++) {
			// determine the input to finalPerceptron
			double input = this.attributes.get(i).has(row) ? 1.0 : -1.0;
			double expectedOutput = mapOutputToDouble(row.outputValue);
			double update = learningRate * input * expectedOutput;
			totalUpdates += Math.abs(update);
			finalPerceptron.weights[i] += update;
		}
		return totalUpdates;
	}

	/***
	 * Calculates the values for the inputs, based on the attributes.
	 * True is mapped to 1, false is mapped to -1.
	 * @param row The input to test the attributes on.
	 * @return A double array representing the activations on the input nodes
	 */
	private double[] calculateInputValues(InputRow row) {
		// true -> 1.0;
		// false -> -1.0;
		// calculate the values as they go through the network
		double[] inputsConverted = new double[numberAttributes];
		for (int k = 0; k < numberAttributes; k++) {
			inputsConverted[k] = attributes.get(k).has(row) ? 1.0 : -1.0;
		}
		return inputsConverted;
	}

	/** Maps the language to either 1.0 (first language) or -1.0 (second language) */
	private double mapOutputToDouble(String language) {
		return language.equals(languageOne) ? 1.0 : -1.0;
	}

	@Override
	public LanguageDecision decide(InputRow row) {

		// need a working array of doubles for the calculations through, initially the inputs
		double[] inputs = calculateInputValues(row);
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
