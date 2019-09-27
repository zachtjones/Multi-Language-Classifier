package examples;

import helper.Pair;
import main.Learning;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used solely to obtain a set of inputs from the several different languages.
 */
public class GetWikipediaContent {

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

                if (!i.trim().equals("")) {
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
        Scanner sc = new Scanner(url.openStream());
        StringBuilder results = new StringBuilder();
        while (sc.hasNextLine()) {
            results.append(sc.nextLine());
            results.append('\n');
        }
        return results.toString();
    }

    /** loads the random articles from Wikipedia, saving to the output file */
    public static void main(String outFileName, int numberExamplesEach) throws IOException {

        List<Pair<String, URL>> urls = Learning.getLanguageUrls();

        PrintWriter pw = new PrintWriter(outFileName);

        for (Pair<String, URL> url : urls) {
            final String language = url.one;
            final URL currentURL = url.two;

            int counter = 0;
            while (counter < numberExamplesEach) {
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

}
