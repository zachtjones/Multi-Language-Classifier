package learners;

import com.zachjones.languageclassifier.entities.InputRow;
import com.zachjones.languageclassifier.entities.LanguageDecision;
import com.zachjones.languageclassifier.model.types.Language;

/***
 * Represents a decider with a percentage of confidence known that is not 0 or 1.
 * This is used to distinguish a pair only.
 */
public class ConfidenceDecider extends LanguageDecision implements Decider {

    private final Language languageOne, languageTwo;
    private final double fractionOne;

    /**
     * Creates a LanguageDecision based on the confidence between the two langauges.
     * @param languageOne The first language
     * @param languageTwo The second language
     * @param fractionOne The fraction that is the first language (in range [0.0, 1.0])
     */
    public ConfidenceDecider(Language languageOne, Language languageTwo, double fractionOne) {
        this.languageOne = languageOne;
        this.languageTwo = languageTwo;
        this.fractionOne = fractionOne;
        if (Double.isNaN(fractionOne)) {
            throw new IllegalArgumentException("Confidence decider can not be NaN");
        }
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
    public double confidenceForLanguage(Language language) {
        if (language.equals(languageOne)) {
            return fractionOne;
        } else if (language.equals(languageTwo)){
            return 1 - fractionOne;
        } else {
            return 0;
        }
    }

    @Override
    public Language mostConfidentLanguage() {
        return fractionOne > 0.5 ? languageOne : languageTwo;
    }
}
