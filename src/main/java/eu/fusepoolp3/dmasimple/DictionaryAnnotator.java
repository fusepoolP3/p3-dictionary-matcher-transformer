package eu.fusepoolp3.dmasimple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.arabidopsis.ahocorasick.SearchResult;
import org.arabidopsis.ahocorasick.AhoCorasick;
import org.tartarus.snowball.SnowballStemmer;

/**
 *
 * @author Gábor Reményi
 */
public class DictionaryAnnotator {
    // contains the search tree 

    private AhoCorasick tree;
    // OpenNLP tokenizer class
    private TokenizerModel modelTok;
    private Tokenizer tokenizer;
    private ProcessedText processedText;
    private List<ProcessedText> processedTerms;
    private DictionaryStore dictionary;
    private DictionaryStore originalDictionary;
    private DictionaryStore processedDictionary;
    private List<Annotation> entities;
    private boolean caseSensitive;
    private int caseSensitiveLength;
    private boolean eliminateOverlapping;
    private String stemmingLanguage;
    private Boolean stemming;
    private Map<String, String> languages;

    /**
     * Initializes the dictionary annotator by reading the dictionary and
     * building the search tree which is the soul of the Aho-Corasic algorithm.
     *
     * @param dictionary
     * @param _tokenizer
     * @param _stemmingLanguage
     * @param _caseSensitive
     * @param _caseSensitiveLength
     * @param _eliminateOverlapping
     */
    public DictionaryAnnotator(DictionaryStore _dictionary, String _stemmingLanguage, boolean _caseSensitive,
            int _caseSensitiveLength, boolean _eliminateOverlapping) {

        dictionary = _dictionary;
        stemmingLanguage = _stemmingLanguage;
        caseSensitive = _caseSensitive;
        caseSensitiveLength = _caseSensitiveLength;
        eliminateOverlapping = _eliminateOverlapping;
        
        URI uri = null;
        try {
            // get tokenizer from resources
            uri = this.getClass().getResource("/en-token.bin").toURI();
        } catch (URISyntaxException ex) {
            Logger.getLogger(DictionaryAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        }

        // loading opennlp tokenizer model
        try {
            modelTok = new TokenizerModel(new File(uri));
            tokenizer = new TokenizerME(modelTok);
        } catch (FileNotFoundException ex) {
            System.err.println("Error while loading tokenizer model: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("Error while loading tokenizer model: " + ex.getMessage());
        }
        if (tokenizer == null) {
            System.err.println("Tokenizer cannot be NULL");
        }

        // if no stemming language configuration is provided set stemming language to None
        if (stemmingLanguage == null || stemmingLanguage.isEmpty()) {
            stemmingLanguage = "None";
        }
        // create a mapping between the language and the name of the class
        // responsible for the stemming of the current language
        languages = new HashMap<>();
        languages.put("None", "");
        languages.put("Danish", "danishStemmer");
        languages.put("Dutch", "dutchStemmer");
        languages.put("English", "englishStemmer");
        languages.put("Finnish", "finnishStemmer");
        languages.put("French", "frenchStemmer");
        languages.put("German", "germanStemmer");
        languages.put("Hungarian", "hungarianStemmer");
        languages.put("Italian", "italianStemmer");
        languages.put("Norwegian", "norwegianStemmer");
        //languages.put("english2", "porterStemmer");
        languages.put("Portuguese", "portugueseStemmer");
        languages.put("Romanian", "romanianStemmer");
        languages.put("Russian", "russianStemmer");
        languages.put("Spanish", "spanishStemmer");
        languages.put("Swedish", "swedishStemmer");
        languages.put("Turkish", "turkishStemmer");

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
        String[] terms = ReadDictionary();

        // tokenize terms in the dictionary
        TokenizeTerms(terms);

        tree = new AhoCorasick();

        // if stemming language was set, perform stemming of terms in the dictionary
        if (stemming) {
            StemTerms();
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
     * Processes the input text and returns the found entities.
     *
     * @param text Input text on which the dictionary matching is executed
     * @return
     */
    public List<Annotation> GetEntities(String text) {
        long start, end;
        System.out.print("Extracting entities from input text ");
        start = System.currentTimeMillis();

        // tokenize text
        TokenizeText(text);

        // if stemming language was set, perform stemming of the input text
        if (stemming) {
            StemText();
        }

        // perform the look-up
        FindEntities();

        // eliminate overlapping entities
        EliminateOverlapping();

        List<Annotation> entitiesToReturn = new ArrayList<>();
        for (Annotation e : entities) {
            if (!e.overlap) {
                entitiesToReturn.add(e);
            }
        }


        System.out.print("(" + entitiesToReturn.size() + ") ...");

        end = System.currentTimeMillis();
        System.out.println(" done [" + Double.toString((double) (end - start) / 1000) + " sec] .");

        return entitiesToReturn;
    }

    /**
     * Processes the input text and returns a tagged text.
     *
     * @param text
     * @return
     */
    public String GetURITaggedText(String text) {
        String taggedText = "";
        String plain, tagged;

        // tokenize text
        TokenizeText(text);

        // if stemming language was set, perform stemming of the input text
        if (stemming) {
            StemText();
        }

        // perform the look-up
        FindEntities();

        // eliminate overlapping entities
        EliminateOverlapping();

        int prevEnd = 0;
        for (Annotation e : entities) {
            if (!e.isOverlap()) {
                if (e.getBegin() < prevEnd && prevEnd != 0) {
                    if (e.getEnd() > prevEnd) {
                        prevEnd = e.getEnd();
                    }
                } else {
                    plain = text.substring(prevEnd, e.getBegin());
                    tagged = "<entity uri=\"" + e.getUri() + "\">" + text.substring(e.getBegin(), e.getEnd()) + "</entity>";
                    taggedText += plain + tagged;
                    prevEnd = e.getEnd();
                }
            }
        }
        taggedText += text.substring(prevEnd, text.length());
        return taggedText;
    }

    /**
     * Creates the dictionary from the HashMap which contains label-URI pairs.
     *
     * @param input The original dictionary as a HashMap (label-URI pairs)
     * @return
     */
    private String[] ReadDictionary() {
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
            originalDictionary.AddElement(label, concept);
            index++;
        }
        return labels;
    }

    /**
     * Tokenizes the all the entities in the dictionary and returns the
     * tokenized entities. If caseSensitive is true and caseSensitiveLength > 0
     * all tokens whose length is equal or bigger than the caseSensitiveLength
     * are converted to lowercase. If caseSensitive is true and
     * caseSensitiveLength = 0 no conversion is applied. If caseSensitive is
     * false all tokens are converted to lowercase.
     *
     * @param originalTerms
     */
    public void TokenizeTerms(String[] originalTerms) {
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
            if (caseSensitive) {
                if (caseSensitiveLength > 0) {
                    for (Span span : spans) {
                        word = terms[i].substring(span.getStart(), span.getEnd());

                        if (word.length() > caseSensitiveLength) {
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
                } else {
                    for (Span span : spans) {
                        word = terms[i].substring(span.getStart(), span.getEnd());

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
                }
            } else {
                for (Span span : spans) {
                    word = terms[i].substring(span.getStart(), span.getEnd());

                    word = word.toLowerCase();

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
            }


            tokText.setTokenizedText(sb.toString());
            processedTerms.add(tokText);
        }
    }

    /**
     * Tokenizes the original text and returns the tokenized text. If
     * caseSensitive is true and caseSensitiveLength > 0 all tokens whose length
     * is equal or bigger than the caseSensitiveLength are converted to
     * lowercase. If caseSensitive is true and caseSensitiveLength = 0 no
     * conversion is applied. If caseSensitive is false all tokens are converted
     * to lowercase.
     *
     * @param text
     */
    public void TokenizeText(String text) {
        Span[] spans;
        StringBuilder sb;

        processedText = new ProcessedText(text);

        spans = tokenizer.tokenizePos(text);
        sb = new StringBuilder();

        Token t;
        String word;
        int position = 0;
        int begin, end;

        sb.append(" ");
        if (caseSensitive) {
            if (caseSensitiveLength > 0) {
                for (Span span : spans) {
                    word = text.substring(span.getStart(), span.getEnd());

                    if (word.length() > caseSensitiveLength) {
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

                    processedText.addToken(t);

                    sb.append(word);
                    sb.append(" ");
                }
            } else {
                for (Span span : spans) {
                    word = text.substring(span.getStart(), span.getEnd());
                    t = new Token(word);
                    t.setOriginalBegin(span.getStart());
                    t.setOriginalEnd(span.getEnd());

                    begin = position + 1;
                    t.setBegin(begin);

                    end = begin + word.length();
                    t.setEnd(end);

                    position = end;

                    processedText.addToken(t);

                    sb.append(word);
                    sb.append(" ");
                }
            }
        } else {
            for (Span span : spans) {
                word = text.substring(span.getStart(), span.getEnd());

                word = word.toLowerCase();

                t = new Token(word);
                t.setOriginalBegin(span.getStart());
                t.setOriginalEnd(span.getEnd());

                begin = position + 1;
                t.setBegin(begin);

                end = begin + word.length();
                t.setEnd(end);

                position = end;

                processedText.addToken(t);

                sb.append(word);
                sb.append(" ");
            }
        }
        processedText.setTokenizedText(sb.toString());
    }

    /**
     * This function runs the Aho-Corasick string matching algorithm on the
     * tokenized (and stemmed) text using the search tree built from the
     * dictionary.
     *
     * @param pt The tokenized text
     */
    private void FindEntities() {
        entities = new ArrayList<>();
        String text = stemming ? processedText.stemmedText : processedText.tokenizedText;
        Annotation entity;
        String str = "";
        Concept concept;
        int begin, end, length, lastIndex, maxlength;
        for (Iterator iter = tree.search(text.toCharArray()); iter.hasNext();) {
            SearchResult result = (SearchResult) iter.next();
            maxlength = 0;
            for (Object e : result.getOutputs()) {
                length = e.toString().length();
                if (maxlength < length) {
                    str = e.toString();
                    maxlength = length;
                }
            }
            if (!str.equals("")) {
                str = str.substring(1, str.length() - 1);
                length = str.length();
                end = result.getLastIndex() - 1;
                begin = end - length;

                entity = processedText.FindMatch(begin, end);
                entity.setTokenizedBegin(begin);
                entity.setTokenizedEnd(end);
                entity.uri = stemming ? processedDictionary.GetURI(str) : originalDictionary.GetURI(entity.label);

                if (entity.getUri() != null) {

                    concept = stemming ? processedDictionary.GetConcept(str) : originalDictionary.GetConcept(entity.label);

                    if (concept.IsPrefLabel()) {
                        entity.prefLabel = concept.labelText;
                        entity.altLabel = null;
                    } else {
                        entity.prefLabel = dictionary.GetPrefLabel(entity.getUri());
                        entity.altLabel = concept.labelText;
                    }

                    entity.type = concept.type;

                    entities.add(entity);
                }
            }
        }
    }

    /**
     * Eliminates the overlaps among all the entities found in the text. If we
     * have two entities, Entity1 and Entity2, and Entity1 is within the
     * boundaries of Entity2, then Entity1 is marked and is later discarded. If
     * the variable eliminateOverlapping is true, entities whose boundaries are
     * overlapping are also marked and are later discarded.
     */
    public void EliminateOverlapping() {
        Annotation e1, e2;
        for (int i = 0; i < entities.size(); i++) {
            e1 = entities.get(i);
            for (int j = 0; j < entities.size(); j++) {
                e2 = entities.get(j);
                if (i == j) {
                    continue;
                } else {
                    if (e1.getBegin() > e2.getEnd()) {
                        continue;
                    } else if (e1.getEnd() < e2.getBegin()) {
                        continue;
                    } else {
                        if (e1.getBegin() >= e2.getBegin() && e1.getEnd() <= e2.getEnd()) {
                            e1.overlap = true;
                            break;
                        } else if (eliminateOverlapping) {
                            if (e1.getBegin() > e2.getBegin() && e1.getEnd() > e2.getEnd() && e1.getBegin() < e2.getEnd()) {
                                e1.overlap = true;
                                break;
                            } else if (e1.getBegin() < e2.getBegin() && e1.getEnd() < e2.getEnd() && e1.getEnd() < e2.getBegin()) {
                                e1.overlap = true;
                                break;
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
    }

    /**
     * The function is responsible for the stemming of each entity in the
     * dictionary based on the stemming language defined in the constructor.
     */
    private void StemTerms() {
        try {
            int offset, overallOffset = 0;
            String word, name, uri, type;
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
                concept = dictionary.GetConcept(pt.originalText);
                pt.setStemmedText(name);

                processedDictionary.AddElement(name.substring(1, name.length() - 1), concept);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * The function is responsible for the stemming of the main text based on
     * the stemming language defined in the constructor.
     */
    private void StemText() {
        try {
            int offset, overallOffset = 0;
            String word;
            StringBuilder sb;

            Class stemClass = Class.forName("org.tartarus.snowball.ext." + stemmingLanguage);
            SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();

            sb = new StringBuilder();
            sb.append(" ");
            for (Token token : processedText.tokens) {
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
            processedText.setStemmedText(sb.toString());

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
