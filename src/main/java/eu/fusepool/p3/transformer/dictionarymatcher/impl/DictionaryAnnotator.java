package eu.fusepool.p3.transformer.dictionarymatcher.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.apache.commons.lang.StringUtils;
import org.arabidopsis.ahocorasick.AhoCorasick;
import org.tartarus.snowball.SnowballStemmer;

/**
 *
 * @author Gábor Reményi
 */
public class DictionaryAnnotator {
    // contains the search tree

    public final AhoCorasick tree;
    // OpenNLP tokenizer class
    private List<ProcessedText> processedTerms;
    public DictionaryStore dictionary;
    public final DictionaryStore originalDictionary;
    public final DictionaryStore processedDictionary;
    public boolean caseSensitive;
    public int caseSensitiveLength;
    public String stemmingLanguage;
    public Boolean stemming;
    private final Map<String, String> languages;

    /**
     * Initializes the dictionary annotator by reading the dictionary and building the search tree which is the soul of
     * the Aho-Corasic algorithm.
     *
     * @param _dictionary
     * @param _stemmingLanguage
     * @param _caseSensitive
     * @param _caseSensitiveLength
     */
    public DictionaryAnnotator(DictionaryStore _dictionary, String _stemmingLanguage, boolean _caseSensitive, int _caseSensitiveLength) {

        dictionary = _dictionary;
        stemmingLanguage = _stemmingLanguage;
        caseSensitive = _caseSensitive;
        caseSensitiveLength = _caseSensitiveLength;

        // loading opennlp tokenizer model
        Tokenizer tokenizer = null;
        try {
            InputStream inputStream = this.getClass().getResourceAsStream("/en-token.bin");
            TokenizerModel modelTok = new TokenizerModel(inputStream);
            tokenizer = new TokenizerME(modelTok);
        } catch (IOException e) {
            System.err.println("Error while loading tokenizer model: " + e.getMessage());
            throw new RuntimeException(e);
        }

        // if no stemming language configuration is provided set stemming language to None
        if (StringUtils.isBlank(stemmingLanguage)) {
            stemmingLanguage = "none";
        }
        // create a mapping between the language and the name of the class
        // responsible for the stemming of the current language
        languages = new HashMap<>();
        languages.put("none", "");
        languages.put("danish", "danishStemmer");
        languages.put("dutch", "dutchStemmer");
        languages.put("english", "englishStemmer");
        languages.put("finnish", "finnishStemmer");
        languages.put("french", "frenchStemmer");
        languages.put("german", "germanStemmer");
        languages.put("hungarian", "hungarianStemmer");
        languages.put("italian", "italianStemmer");
        languages.put("norwegian", "norwegianStemmer");
        languages.put("english2", "porterStemmer");
        languages.put("portuguese", "portugueseStemmer");
        languages.put("romanian", "romanianStemmer");
        languages.put("russian", "russianStemmer");
        languages.put("spanish", "spanishStemmer");
        languages.put("swedish", "swedishStemmer");
        languages.put("turkish", "turkishStemmer");

        originalDictionary = new DictionaryStore();
        processedDictionary = new DictionaryStore();

        stemming = false;
        if (stemmingLanguage != null) {
            if (!languages.get(stemmingLanguage).isEmpty()) {
                stemmingLanguage = languages.get(stemmingLanguage);
                stemming = true;
            }
        }

        // read labels from the input dictionary
        String[] terms = readDictionary();

        // tokenize terms in the dictionary
        tokenizeTerms(tokenizer, terms);

        tree = new AhoCorasick();

        // if stemming language was set, perform stemming of terms in the dictionary
        if (stemming) {
            stemTerms();
            // add each term to the seachtree
            for (ProcessedText e : processedTerms) {
                tree.add(e.stemmedText, e.stemmedText);
            }
        } else {
            // add each term to the seachtree
            for (ProcessedText e : processedTerms) {
                tree.add(e.tokenizedText, e.tokenizedText);
            }
        }

        // create search trie
        tree.prepare();
    }

    /**
     * Creates the dictionary from the HashMap which contains label-URI pairs.
     *
     * @param input The original dictionary as a HashMap (label-URI pairs)
     * @return
     */
    private String[] readDictionary() {
        String[] labels = new String[dictionary.keywords.size()];
        Set set = dictionary.keywords.entrySet();
        Iterator iterator = set.iterator();

        int index = 0;
        String label;
        Concept concept;
        while (iterator.hasNext()) {
            Map.Entry me = (Map.Entry) iterator.next();
            label = (String) me.getKey();
            concept = (Concept) me.getValue();
            labels[index] = label;
            originalDictionary.addElement(label, concept);
            index++;
        }
        return labels;
    }

    /**
     * Tokenizes the all the entities in the dictionary and returns the tokenized entities. If caseSensitive is true and
     * caseSensitiveLength > 0 all tokens whose length is equal or bigger than the caseSensitiveLength are converted to
     * lowercase. If caseSensitive is true and caseSensitiveLength = 0 no conversion is applied. If caseSensitive is
     * false all tokens are converted to lowercase.
     *
     * @param tokenizer
     * @param originalTerms
     */
    public void tokenizeTerms(Tokenizer tokenizer, String[] originalTerms) {
        StringBuilder sb;
        Span[] spans;
        String[] terms = originalTerms;

        processedTerms = new ArrayList<>();
        ProcessedText tokText;
        for (int i = 0; i < originalTerms.length; i++) {
            tokText = new ProcessedText(originalTerms[i]);

            spans = tokenizer.tokenizePos(terms[i]);
            sb = new StringBuilder();

            Token t;
            String word;
            int position = 1;
            int begin, end;
            sb.append(" ");

            for (Span span : spans) {
                word = terms[i].substring(span.getStart(), span.getEnd());
                if (caseSensitive) {
                    if (caseSensitiveLength > 0) {
                        if (word.length() > caseSensitiveLength) {
                            word = word.toLowerCase();
                        }
                    }
                } else {
                    word = word.toLowerCase();
                }

                t = new Token(word);
                t.setOriginalBegin(span.getStart());
                t.setOriginalEnd(span.getEnd());

                begin = position + 1;
                t.setBegin(begin);

                end = begin + word.length();
                t.setEnd(end);

                position = end;

                tokText.addToken(t);

                sb.append(word);
                sb.append(" ");
            }

            tokText.setTokenizedText(sb.toString());
            processedTerms.add(tokText);
        }
    }

    /**
     * The function is responsible for the stemming of each entity in the dictionary based on the stemming language
     * defined in the constructor.
     */
    private void stemTerms() {
        try {
            int offset, overallOffset = 0;
            String word, name, uri;
            Concept concept;
            StringBuilder sb;

            Class stemClass = Class.forName("org.tartarus.snowball.ext." + stemmingLanguage);
            SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();

            for (ProcessedText pt : processedTerms) {
                sb = new StringBuilder();
                sb.append(" ");
                for (Token token : pt.tokens) {
                    stemmer.setCurrent(token.text);
                    stemmer.stem();
                    word = stemmer.getCurrent();

                    offset = token.text.length() - word.length();

                    token.begin -= overallOffset;
                    overallOffset += offset;
                    token.end -= overallOffset;

                    sb.append(word);
                    sb.append(" ");

                    token.stem = word;
                }
                name = sb.toString();
                concept = originalDictionary.getConcept(pt.originalText);
                pt.setStemmedText(name);

                processedDictionary.addElement(name.substring(1, name.length() - 1), concept);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
