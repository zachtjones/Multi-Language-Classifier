package learners;

import com.zachjones.languageclassifier.entities.InputRow;

import java.util.HashMap;
import java.util.Map;

/** Represents an absolute decision on a language.
 * This can be used as part of decision trees or other algorithms. */
public class AbsoluteDecider extends LanguageDecision implements Decider {

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
	public LanguageDecision decide(InputRow row) {
		return this;
	}

	@Override
	public String representation(int numSpaces) {
		return "return " + language + ", 1.0 confidence";
	}

	@Override
	public double confidenceForLanguage(String language) {
		if (this.language.equals(language)) return 1.0;
		return 0;
	}

	@Override
	public String mostConfidentLanguage() {
		return this.language;
	}
}
