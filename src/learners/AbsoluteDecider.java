package learners;

import main.InputRow;

import java.util.HashMap;
import java.util.Map;

/** Represents an absolute decision on a language.
 * This can be used as part of decision trees or other algorithms. */
public class AbsoluteDecider implements Decider {

	/** The language to always pick */
	private final String language;

	private static final Map<String, AbsoluteDecider> deciders = new HashMap<>();

	private AbsoluteDecider(String language) {
		this.language = language;
	}

	/** Returns the decider to always decide input is the language specified.
	 * Uses a cache to reduce object creations. */
	public static AbsoluteDecider fromLanguage(String language) {
		if (!deciders.containsKey(language)) {
			deciders.put(language, new AbsoluteDecider(language));
		}
		return deciders.get(language);
	}

	@Override
	public String decide(InputRow row) {
		return language;
	}

	@Override
	public String representation(int numSpaces) {
		return "return " + language;
	}
}
