package learners;

import com.zachjones.languageclassifier.entities.InputRow;
import com.zachjones.languageclassifier.entities.LanguageDecision;
import com.zachjones.languageclassifier.entities.SingleLanguageDecision;
import com.zachjones.languageclassifier.model.types.Language;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** Represents an absolute decision on a language.
 * This can be used as part of decision trees or other algorithms. */
public class AbsoluteDecider implements Decider {

	/** The language to always pick */
	private final SingleLanguageDecision language;

	private static final EnumMap<Language, AbsoluteDecider> deciders = new EnumMap<>(Language.class);

	private AbsoluteDecider(SingleLanguageDecision language) {
		this.language = language;
	}

	/** Returns the decider to always decide input is the language specified.
	 * Uses a cache to reduce object creations. */
	public static AbsoluteDecider fromLanguage(Language language) {
		if (!deciders.containsKey(language)) {
			deciders.put(language, new AbsoluteDecider(SingleLanguageDecision.Companion.fromLanguage(language)));
		}
		return deciders.get(language);
	}

	@Override
	public LanguageDecision decide(InputRow row) {
		return language;
	}

	@Override
	public String representation(int numSpaces) {
		return "return " + language + ", 1.0 confidence";
	}
}
