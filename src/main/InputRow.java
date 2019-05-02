package main;

import examples.GetWikipediaContent;

import java.io.Serializable;

/**
 * Represents a row of input to either the training or testing.
 * For simplicity this will arbitrarily label unlabeled data to English.
 */
class InputRow implements Serializable {
	final String[] words;
	/** Null if unlabeled data */
	final String outputValue;

	/** Parses the input row from text, whether labeled or unlabeled.
	 * Precondition: text doesn't have the pipe character in it unless to separate label from content. */
	InputRow(String content) {
		final String[] row;
		if (content.contains("|")) {
			row = content.split("\\|");
			this.outputValue = row[0];
		} else {
			// unlabeled, give it something
			row = new String[2];
			row[1] = content;
			this.outputValue = null;
		}
		this.words = row[1]
			.replaceAll(GetWikipediaContent.REGEX, " ")
			.toLowerCase()
			.split(" "); // split on spaces
	}
}
