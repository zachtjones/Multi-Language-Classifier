package attributes;

import main.InputRow;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class WordContainsAttribute extends Attributes {

    private final String contains;
    private final double fitness;

    /** Creates a word start attribute, given what the word must contain,
     * and the inputs and language used to calculate fitness. */
    public WordContainsAttribute(String contains, List<InputRow> inputs, String languageOne) {
        this.contains = contains;
        this.fitness = Attributes.fitness(this, inputs, languageOne);
    }

    @Override
    public boolean has(InputRow input) {
        // iterate through the words to see if one matches exactly
        for (int i = 0; i < input.words.length; i++) {
            if (input.words[i].contains(contains)) return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "a word contains '" + contains + "'";
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public Attributes mutate(List<String> words, List<InputRow> inputs, String languageOne, String languageTwo) {
        Random r = new Random();

        final String newContains;

        // either replace the last character, or append a new character
        // replace the characters only 1/4 of the time
        if (r.nextBoolean() && r.nextBoolean() && contains.length() > 0) {
            // drop either the starting character or the ending character
            if (r.nextBoolean()) {
                newContains = this.contains.substring(1);
            } else {
                newContains = this.contains.substring(0, this.contains.length() - 1);
            }
        } else {
            // appending a new character, either beginning or the end (decided later)
            newContains = this.contains;
        }

        // add another letter on to the suffix, drawn from the pool words with the suffix passing
        List<String> wordsFilter = words.stream()
            .filter(i -> i.contains(newContains))
            .collect(Collectors.toList());

        // nothing passed the filter, that's a bad start, don't want to keep it
        if (wordsFilter.size() == 0) return this;

        // draw a random one from the list
        String random = wordsFilter.get(r.nextInt(wordsFilter.size()));

        // pick either: append or prepend another character, drawn from random word
        if (r.nextBoolean()) {

            // prepend the starting
            int index = random.indexOf(newContains);
            String starting = random.substring(0, index);

            if (starting.length() > 0) {
                // get the last character of what's before the random word
                char newChar = starting.charAt(starting.length() - 1);
                return new WordContainsAttribute(newChar + newContains, inputs, languageOne);
            }
            // word contains the pattern at the start of the word, return this
            return this;

        } else {

            // append the ending
            String ending = random.substring(random.indexOf(newContains) + newContains.length());

            if (ending.length() > 0) {
                // get the first character of what's after the random word
                char newChar = ending.charAt(0);
                return new WordContainsAttribute(newContains + newChar, inputs, languageOne);
            }
            // word contains the pattern at the end of the word, return this
            return this;
        }
    }
}
