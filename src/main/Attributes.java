package main;

import java.io.Serializable;

public interface Attributes extends Serializable {

	/** Method to determine if the attribute is true on this input row. */
	boolean has(InputRow input);

	/** Helper method to make code elsewhere more concise. */
	default boolean doesntHave(InputRow input) {
		return !has(input);
	}

	/** A human-readable name for this attribute. */
	String name();
}
