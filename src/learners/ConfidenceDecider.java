package learners;

import main.InputRow;

/***
 * Represents a decider with a percentage of confidence known that is not 0 or 1.
 * This is used to distinguish a pair only.
 */
public class ConfidenceDecider extends LanguageDecision implements Decider {

    private final String languageOne, languageTwo;
    private final double fractionOne;

    public ConfidenceDecider(String languageOne, String languageTwo, double fractionOne) {
        this.languageOne = languageOne;
        this.languageTwo = languageTwo;
        this.fractionOne = fractionOne;
    }

    @Override
    public LanguageDecision decide(InputRow row) {
        return this;
    }

    @Override
    public String representation(int numSpaces) {
        return "return " + languageOne + " with " + fractionOne + ", languageTwo with " + (1 - fractionOne);
    }

    @Override
    public double confidenceForLanguage(String language) {
        if (language.equals(languageOne)) {
            return fractionOne;
        } else if (language.equals(languageTwo)){
            return 1 - fractionOne;
        } else {
            return 0;
        }
    }

    @Override
    public String mostConfidentLanguage() {
        return fractionOne > 0.5 ? languageOne : languageTwo;
    }
}
