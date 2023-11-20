package main;

import helper.Pair;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Learning {

	/**
	 * Returns a list of pairs of languages and their corresponding URL to use for wikipedia content.
	 * @return The list of Pair&lt;String, URL&gt;
	 */
	public static List<Pair<String, URL>> getLanguageUrls() {
		try {
			return Arrays.asList(
				new Pair<>("Albanian", new URL("https://sq.wikipedia.org/wiki/Speciale:Rast%C3%ABsishme")),
				new Pair<>("Croatia", new URL("https://hr.wikipedia.org/wiki/Posebno:Slu%C4%8Dajna_stranica")),
				new Pair<>("Czech", new URL("https://cs.wikipedia.org/wiki/Speci%C3%A1ln%C3%AD:N%C3%A1hodn%C3%A1_str%C3%A1nka")),
				new Pair<>("Danish", new URL("https://da.wikipedia.org/wiki/Speciel:Tilf%C3%A6ldig_side")),
				new Pair<>("Dutch", new URL("https://nl.wikipedia.org/wiki/Speciaal:Willekeurig")),
				new Pair<>("English", new URL("https://en.wikipedia.org/wiki/Special:Random")),
				new Pair<>("French", new URL("https://fr.wikipedia.org/wiki/Sp%C3%A9cial:Page_au_hasard")),
				new Pair<>("Gaelic", new URL("https://gd.wikipedia.org/wiki/S%C3%B2nraichte:Random")),
				new Pair<>("German", new URL("https://de.wikipedia.org/wiki/Spezial:Zuf%C3%A4llige_Seite")),
				new Pair<>("Hawaiian", new URL("https://haw.wikipedia.org/wiki/Papa_nui:Kaulele")),
				new Pair<>("Icelandic", new URL("https://is.wikipedia.org/wiki/Kerfiss%C3%AD%C3%B0a:Handah%C3%B3fsvalin_s%C3%AD%C3%B0a")),
				new Pair<>("Italian", new URL("https://it.wikipedia.org/wiki/Speciale:PaginaCasuale")),
				new Pair<>("Romanian", new URL("https://ro.wikipedia.org/wiki/Special:Aleatoriu")),
				new Pair<>("Samoan", new URL("https://sm.wikipedia.org/wiki/Special:Random")),
				new Pair<>("Spanish", new URL("https://es.wikipedia.org/wiki/Especial:Aleatoria"))
			);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	/** Holds a list of all the languages. */
	public final static List<String> languages = getLanguageUrls().stream().map(i -> i.one).collect(Collectors.toList());

	/***
	 * Holds all the pairs of languages that could be used, but in alphabetical order for the languages.
	 */
	public final static List<Pair<String, String>> languagePairs;
	static {
		languagePairs = new ArrayList<>();
		// obtain pairwise attributes that are good to split by
		for (int i = 0; i < languages.size(); i++) {
			String firstLanguage = languages.get(i);
			for (int j = i + 1; j < languages.size(); j++) {
				String secondLanguage = languages.get(j);
				languagePairs.add(new Pair<>(firstLanguage, secondLanguage));
			}
		}
	}

	/** Creates a string of length numSpaces comprised of spaces. */
	public static String numSpaces(int numSpaces) {
		return new String(new char[numSpaces]).replace('\0', ' ');
	}
}
