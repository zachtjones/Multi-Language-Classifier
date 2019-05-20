package learners;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;

/***
 * This class is used to represent a perceptron model of neural networks.
 * A Perceptron uses a linear combination of the inputs to determine the result:
 *  result = a + bx + cy + dz + ...,
 * 	where a-d are weights, x-z are input values
 */
class Perceptron implements Serializable {

	final double[] weights;
	final double bias; // the intercept term, never gets adjusted


	/**
	 * Creates a perceptron, given the number of inputs to this node.
	 * The weights are initialized to random values.
	 * @param numberInputs The number of inputs to this node from either other nodes
	 *                     or inputs to the network.
	 */
	Perceptron(int numberInputs) {
		Random r = new Random();

		weights = new double[numberInputs];

		// initialize to random weights in [-1, 1]
		for (int i = 0; i < weights.length; i++) {
			weights[i] = r.nextDouble() * 2 - 1.0; // transform [0, 1] -> [-1, 1]
		}
		bias = r.nextDouble() * 2 - 1.0;
	}

	/**
	 * Calculates the result from the input values, based on what was learned.
	 * @param inputValues The array of input values to this node in the network, must
	 *                    be the same size as the initial numberInputs used in the constructor.
	 * @return A double representing the result of the linear combination of weights,
	 *         this result will be in the range [-1, 1].
	 */
	double calculateResult(double[] inputValues) {
		// learn combination
		double result = bias; // intercept = a

		for (int i = 0; i < inputValues.length; i++) {
			// bx + cy + dz + ...
			result += inputValues[i] * weights[i];
		}

		// max of the sum of absolute values of the weights
		double max = Arrays.stream(weights).map(Math::abs).sum();

		return result / max;
	}
}
