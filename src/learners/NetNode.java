package learners;

import main.InputRow;

import java.io.Serializable;

public interface NetNode extends Serializable {

	/**
	 * Calculates the activation of this node in the network.
	 * @param row The input to test
	 * @return The resulting calculation based on the linear combination of previous nodes.
	 */
	double activation(InputRow row);
}
