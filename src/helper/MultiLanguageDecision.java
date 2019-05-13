package helper;

import learners.LanguageDecision;
import main.Learning;

import java.util.HashMap;
import java.util.Map;

/***
 * Represents a language decision where there's multiple languages or multiple weighted
 * parts in each decision.
 */
public class MultiLanguageDecision extends LanguageDecision {

    private final Map<String, Double> items = new HashMap<>();


    /** Adds weight to the specified language. */
    private void addWeightTo(String item, double weight) {
        // insert into the map if not already there
        if (!items.containsKey(item))
            items.put(item, 0.0);

        // add on the weight
        items.put(item, items.get(item) + weight);
    }

    /**
     * Adds the weight to the overall language decision, with this decision
     * being just as useful as the other ones.
     * @param decide The language decision for the part.
     */
    public void addWeightTo(LanguageDecision decide) {
        addWeightTo(decide, 1.0);
    }

    /**
     * Adds the weight to the overall language decision, with this decision playing
     * just a fraction of the overall decision.
     * @param decide The language decision for the part.
     * @param weight The relative weight of this part's decision.
     */
    public void addWeightTo(LanguageDecision decide, double weight) {
        for (String language : Learning.languages) {
            addWeightTo(language, decide.confidenceForLanguage(language) * weight);
        }
    }

    /**
     * Normalizes the weights so that the sum is 1.0
     */
    public void normalize() {
        double totalWeight = items.values().stream().mapToDouble(i -> i).sum();
        for (String key : items.keySet()) {
            items.put(key, items.get(key) / totalWeight);
        }
    }

    @Override
    public double confidenceForLanguage(String language) {
        if (items.containsKey(language))
            return items.get(language);
        return 0;
    }

    @Override
    public String mostConfidentLanguage() {
        String mostCommon = Learning.languages.get(0);
        double maxConfidence = 0;
        for (String i : Learning.languages) {
            double c = confidenceForLanguage(i);
            if (c > maxConfidence) {
                mostCommon = i;
                maxConfidence = c;
            }
        }
        return mostCommon;
    }
}
