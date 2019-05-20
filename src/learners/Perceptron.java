package learners;

import helper.WeightedList;
import main.InputRow;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/***
 * This class is used to represent a perceptron model of neural networks.
 * A Perceptron uses a linear combination of the inputs to determine the result:
 *  result = a + bx + cy + dz + ...,
 * 	where a-d are weights, x-z are input values
 */
class Perceptron implements NetNode, Serializable {

	private final double bias; // the intercept term, never gets adjusted
	private final WeightedList<NetNode> weights; // previous ones
	private final String languageOne;
	private final double learningRate;

	/**
	 * Creates a perceptron, given the inputs to this node.
	 * The weights are initialized to random values.
	 */
	Perceptron(List<NetNode> parents, String languageOne, double learningRate) {
		Random r = new Random();

		this.languageOne = languageOne;
		this.learningRate = learningRate;
		weights = new WeightedList<>(parents);

		// initialize to random weights in [-1, 1]
		for (int i = 0; i < weights.size(); i++) {
			weights.setWeight(i, r.nextDouble() * 2 - 1.0); // transform [0, 1] -> [-1, 1]
		}
		//bias = r.nextDouble() * 2 - 1.0;
		bias = 0.0;
	}

	@Override
	public double activation(InputRow row) {
		// the linear combination of the parents
		double result = bias; // intercept
		for (int i = 0; i < weights.size(); i++) {
			result += weights.getItem(i).activation(row) * weights.getWeight(i);
		}

		// max is the sum of the absolute values of the weights
		double max = weights.stream().mapToDouble(i -> Math.abs(i.one)).sum();

		return result / max;
	}

	/**
	 * Updates the weights to this node.
	 * @param row The input that was classified incorrectly.
	 * @return The sum of absolute values of the weight changed.
	 */
	double updateWeights(InputRow row) {
		double totalUpdates = 0.0;
		// update the perceptron, don't update the intercept
		for (int i = 0; i < weights.size(); i++) {
			// determine the input to finalPerceptron
			double input = this.weights.getItem(i).activation(row);
			double expectedOutput = row.outputValue.equals(languageOne) ? 1.0 : -1.0;
			double update = learningRate * input * expectedOutput;
			totalUpdates += Math.abs(update);
			// add on the new weight
			this.weights.setWeight(i, weights.getWeight(i) + update);
		}
		return totalUpdates;
	}
}
