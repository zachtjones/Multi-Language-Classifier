package examples;

import helper.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used solely to obtain a set of inputs from the several different languages.
 */
public class GetWikipediaContent {

    /** The output file name for the examples randomly generated. */
    private static final String outFileName = "testing.txt";

    /** The number of examples of each language to generate */
    private static final int NUMBER_EXAMPLES_EACH = 200;

    /** Regular expression of characters to remove from the input. */
    public static final String REGEX = "[\\-()*&^%$#@!,./?'\";:+«»‘\\[\\]{}=_\\\\|\u8211\u0183°′”″“’ʻ·–—•º„]";

    /** constant time removal from start and appending to end. Keep it in case for
     * word order mattering */
    private static HashMap<URL, LinkedList<String>> cache = new HashMap<>();

    /** Returns a list of 20 words for the language described by the URL. */
    private static List<String> downloadFileAndGetWords(URL url) throws IOException {

        // obtain from the cache if possible
        if (cache.containsKey(url) && cache.get(url).size() > 20) {
            // obtain 20 words from the cache
            List<String> words = new LinkedList<>();
            LinkedList<String> cached = cache.get(url);
            for (int i = 0; i < 20; i++) {
                words.add(cached.removeFirst());
            }
            return words;
        }

        // cache is empty, so do the network call to fill the cache
        String content =
            downloadFile(url)
                .replace('\n', ' ') // make one line so regex does the whole string.
                .replaceAll("<script.*?</script>", "") // remove javascript code
                .replaceAll("<style.*?</style>", "") // remove css
                .replaceAll("<!--.*?-->", ""); // remove html comments

        // specifically target paragraphs
        Pattern p = Pattern.compile("<p>(.*?)</p>");
        LinkedList<String> words = new LinkedList<>();

        Matcher m = p.matcher(content);
        while (m.find()) {
            String group = m.group(1);
            String[] items = group
                .replaceAll("<.*?>", " ") // remove html tags
                .replaceAll("[0-9]", "") // remove numbers
                .replaceAll("&.*?;", " ") // remove html special chars
                .replaceAll(REGEX, " ") // punctuation
                .split("\\b");
            for (String i : items) {

                // since all these languages have latin characters, we can drop them if they don't
                boolean hasNonLatinCharacters = false;
                for (int j = 0; j < i.length(); j++) {
                    if (i.charAt(j) > '\u02AF') {
                        hasNonLatinCharacters = true;
                    }
                }

                if (hasNonLatinCharacters) continue; // skip ones with non latin characters

                if (!i.isBlank()) {
                    // don't add names
                    String lower = i.toLowerCase();
                    if (lower.equals(i)) {
                        words.add(i.trim());
                    }
                }
            }
        }

        // fill in the cache
        cache.put(url, words);
        // recursive call, just so that I don't copy and paste from above
        return downloadFileAndGetWords(url);
    }

    /** Downloads the file into the string. */
    private static String downloadFile(URL url) throws IOException {
        // This user agent is for if the server wants real humans to visit
        // This socket type will allow to set user_agent
        URLConnection con = url.openConnection();

        // Requesting input data from server
        InputStream inputStream = con.getInputStream();
        byte[] content = inputStream.readAllBytes();
        inputStream.close();
        return new String(content);
    }

    /** loads the random articles from Wikipedia, saving to the output file */
    public static void main(String[] args) throws IOException {

        List<Pair<String, URL>> urls = getLanguageUrls();

        PrintWriter pw = new PrintWriter(outFileName);

        for (Pair<String, URL> url : urls) {
            final String language = url.one;
            final URL currentURL = url.two;

            int counter = 0;
            while (counter < NUMBER_EXAMPLES_EACH) {
                // language|words, separated with spaces
                pw.print(language);

                pw.print("|");

                // 20 word size here always
                List<String> content = downloadFileAndGetWords(currentURL);
                pw.println(String.join(" ", content));

                counter++;
            }
            System.out.println("Done with: " + language);
        }

        pw.close();
    }

    private static List<Pair<String, URL>> getLanguageUrls() throws MalformedURLException {
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
    }
}
