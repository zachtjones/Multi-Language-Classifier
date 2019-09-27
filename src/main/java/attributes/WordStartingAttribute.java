package attributes;

import main.InputRow;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class WordStartingAttribute extends Attributes {

    private final String start;
    private final double fitness;

    /** Creates a word start attribute, given the start, and the inputs and language used to calculate fitness. */
    public WordStartingAttribute(String start, List<InputRow> inputs, String languageOne) {
        this.start = start;
        this.fitness = Attributes.fitness(this, inputs, languageOne);
    }

    @Override
    public boolean has(InputRow input) {
        // iterate through the words to see if one matches exactly
        for (int i = 0; i < input.words.length; i++) {
            if (input.words[i].startsWith(start)) return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "a word starts with '" + start + "'";
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public Attributes mutate(List<String> words, List<InputRow> inputs, String languageOne, String languageTwo) {
        Random r = new Random();

        final String newStarting;

        // either replace the last character, or append a new character
        // replace the characters only 1/4 of the time
        if (r.nextBoolean() && r.nextBoolean() && start.length() > 0) {
            // replace the last letter of the suffix, so you drop the last character
            newStarting = this.start.substring(0, this.start.length() - 1);
        } else {
            // appending a new character
            newStarting = this.start;
        }

        // add another letter on to the suffix, drawn from the pool words with the suffix passing
        List<String> wordsFilter = words.stream()
            .filter(i -> i.startsWith(newStarting))
            .collect(Collectors.toList());

        // nothing passed the filter, that's a bad start, don't want to keep it
        if (wordsFilter.size() == 0) return this;

        // draw a random one from the list
        String random = wordsFilter.get(r.nextInt(wordsFilter.size()));

        String rest = random.substring(newStarting.length());
        if (rest.length() > 1) {
            char extra = rest.charAt(0);
            return new WordStartingAttribute(newStarting + extra, inputs, languageOne);
        }
        // if there weren't any extra characters left on the word, just return this
        return this;
    }
}
