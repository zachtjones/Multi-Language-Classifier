package attributes;

import main.InputRow;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class WordEndingAttribute extends Attributes {

    private final String ending;
    private final double fitness;

    /** Creates a word ending attribute, given the ending, and the inputs and language used to calculate fitness. */
    public WordEndingAttribute(String ending, List<InputRow> inputs, String languageOne) {
        this.ending = ending;
        this.fitness = Attributes.fitness(this, inputs, languageOne);
    }

    @Override
    public boolean has(InputRow input) {
        // iterate through the words to see if one matches exactly
        for (int i = 0; i < input.words.length; i++) {
            if (input.words[i].endsWith(ending)) return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "word ends with '" + ending + "'";
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public Attributes mutate(List<String> words, List<InputRow> inputs, String languageOne, String languageTwo) {
        Random r = new Random();

        final String newEnding;

        // either replace the first character, or append a new character
        // replace the characters only 1/4 of the time
        if (r.nextBoolean() && r.nextBoolean() && ending.length() > 0) {
            // replace the first letter of the suffix, so you drop the first character
            newEnding = this.ending.substring(1);
        } else {
            // appending a new character
            newEnding = this.ending;
        }

        // add another letter on to the suffix, drawn from the pool words with the suffix passing
        List<String> wordsFilter = words.stream()
            .filter(i -> i.endsWith(newEnding))
            .collect(Collectors.toList());

        // nothing passed the filter, that's a bad ending, don't want to keep it
        if (wordsFilter.size() == 0) return this;

        // draw a random one from the list
        String random = wordsFilter.get(r.nextInt(wordsFilter.size()));
        String rest = random.substring(0, random.lastIndexOf(newEnding));
        if (rest.length() > 1) {
            char extra = rest.charAt(rest.length() - 1);
            return new WordEndingAttribute(extra + ending, inputs, languageOne);
        }
        // if there weren't any extra characters left on the word, just return this
        return this;
    }
}
