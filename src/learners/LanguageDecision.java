package learners;

import main.Learning;

import java.util.Comparator;
import java.util.stream.Collectors;

/***
 * Represents a probability distribution of a language choice.
 * This factors in the confidence of the binary decision on each element
 * to make the multi-classifier more accurate.
 */
public abstract class LanguageDecision {

    /** Returns the confidence for a language choice.
     * This should be a number between 0 (definitely not the language) to 1.0 (certainly the language)
     * The sum for all languages in the set defined should be 1.0 */
    public abstract double confidenceForLanguage(String language);

    /**
     * Returns the most confident language.
     */
    public abstract String mostConfidentLanguage();

    @Override
    public String toString() {
        final String mostCommon = mostConfidentLanguage();
        // sort these by the most probable
        final String confidences = Learning.languages.stream()
            .sorted(Comparator.comparingDouble(this::confidenceForLanguage).reversed())
            .map(i -> String.format("%s=%.1f%%", i, confidenceForLanguage(i) * 100.0))
            .collect(Collectors.joining("\n"));
        return "Phrase is probably: " + mostCommon + "\n\nProbabilities:\n" + confidences;
    }
}
