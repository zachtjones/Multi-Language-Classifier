package attributes;

import main.InputRow;

import java.util.List;
import java.util.Random;

/**
 * These attributes split the results based on letter frequency of one letter more than another one.
 */
public class LetterFrequencyAttribute extends Attributes {

    private final char more, less;
    private final double fitness;

    public LetterFrequencyAttribute(char more, char less,
                                    List<InputRow> inputs, String languageOne, String languageTwo) {
        this.more = more;
        this.less = less;
        this.fitness = fitness(this, inputs, languageOne, languageTwo);
    }

    @Override
    public boolean has(InputRow input) {
        int moreCount = 0;
        int lessCount = 0;

        for (int i = 0; i < input.words.length; i++) {
            String wordI = input.words[i];
            for (int j = 0; j < wordI.length(); j++) {
                char letter = wordI.charAt(j);
                if (letter == more) moreCount++;
                if (letter == less) lessCount++;
            }
        }

        return moreCount > lessCount;
    }

    @Override
    public String name() {
        return "words have more '" + more + "' than '" + less + "'";
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public Attributes mutate(List<String> words, List<InputRow> inputs, String languageOne, String languageTwo) {
        // pick a random letter of a random word and replace one of this fields with it
        Random r = new Random();
        String word = words.get(r.nextInt(words.size()));
        char letter = word.charAt(r.nextInt(word.length()));
        if (r.nextBoolean()) {
            return new LetterFrequencyAttribute(this.more, letter, inputs, languageOne, languageTwo);
        } else {
            return new LetterFrequencyAttribute(letter, this.less, inputs, languageOne, languageTwo);
        }
    }
}
