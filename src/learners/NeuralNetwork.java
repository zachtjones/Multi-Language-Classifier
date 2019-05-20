package learners;

import attributes.Attributes;
import main.InputRow;

import java.util.ArrayList;
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


	/** Output node, holds a parent reference to all the nodes it needs */
	private final Perceptron output; // the one in the output layer

	private final static double learningRate = 0.005;


	public NeuralNetwork(List<InputRow> rows, int hiddenLayers, int nodesPerLayer,
										Set<Attributes> attributes, String languageOne, String languageTwo) {

		// build up the graph
		List<NetNode> currentParents = new ArrayList<>(attributes);
		List<NetNode> nextIteration;

		for (int i = 0; i < hiddenLayers; i++) {
			nextIteration = new ArrayList<>(nodesPerLayer);
			for (int j = 0; j < nodesPerLayer; j++) {
				nextIteration.add(new Perceptron(currentParents, languageOne, learningRate));
			}
			// prepare for next iteration
			currentParents = nextIteration;
		}

		// final perceptron
		output = new Perceptron(currentParents, languageOne, learningRate);


		this.rows = rows;
		this.hiddenLayers = hiddenLayers;
		this.nodesPerLayer = nodesPerLayer;
		this.attributes = new ArrayList<>(attributes); // need an order since we have arrays
		this.numberAttributes = attributes.size();

		this.languageOne = languageOne;
		this.languageTwo = languageTwo;

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
				totalUpdates += updateWeightsForInput(row);
			}
		}
		return totalUpdates;
	}

	/** Updates the weights for a specific example.
	 * This is only called for examples that are classified incorrectly */
	private double updateWeightsForInput(InputRow row) {

		// it's already determined that this classification is incorrect, relay the error back
		//  throughout the network

		// have to iterate backwards so we don't calculate things twice
		// responsibility for these goes back from the final node

		// first step: calculate all the activations:
		// need a working array of doubles for the calculations through, initially the inputs
		/*double[][] activations = new double[hiddenLayers][];
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
		}*/

		return output.updateWeights(row);
	}

	@Override
	public LanguageDecision decide(InputRow row) {

		double result = output.activation(row); // range [-1.0, 1.0]
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

		// TODO have this print out all the weights

		return result.toString();
	}
}
