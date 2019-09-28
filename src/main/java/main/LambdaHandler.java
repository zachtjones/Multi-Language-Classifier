package main;

import learners.Decider;
import learners.LanguageDecision;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LambdaHandler {

    /** Returns the JSON of language -> probability */
    public Map<String, Double> handleRequest(Map<String, String> options) throws IOException, ClassNotFoundException {
        System.out.println("Received options: " + options);
        System.out.println("Options[phrase]: " + options.get("phrase"));
        Decider decider = Decider.loadFromResources("learnerOut.dat");
        String phrase = options.get("phrase");
        System.out.println("Loaded learner.");
        LanguageDecision decision = decider.decide(new InputRow(phrase));
        System.out.println("Analyzed the phrase submitted.");
        HashMap<String, Double> response = new HashMap<>();
        Learning.languages.forEach(language -> response.put(language, decision.confidenceForLanguage(language)));
        return response;
    }
}
