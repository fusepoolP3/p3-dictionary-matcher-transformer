package eu.fusepool.p3.transformer.dictionarymatcher.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.arabidopsis.ahocorasick.AhoCorasick;
import org.arabidopsis.ahocorasick.SearchResult;
import org.tartarus.snowball.SnowballStemmer;

/**
 *
 * @author Gabor
 */
public class Extractor {

    private AhoCorasick tree;
    private DictionaryStore dictionary;
    private DictionaryStore originalDictionary;
    private DictionaryStore processedDictionary;
    private boolean caseSensitive;
    private int caseSensitiveLength;
    private boolean eliminateOverlapping = false;
    private String stemmingLanguage;
    private Boolean stemming;

    public Extractor(DictionaryAnnotator da) {
        this.tree = da.tree;
        this.dictionary = da.dictionary;
        this.originalDictionary = da.originalDictionary;
        this.processedDictionary = da.processedDictionary;
        this.caseSensitive = da.caseSensitive;
        this.caseSensitiveLength = da.caseSensitiveLength;
        this.stemmingLanguage = da.stemmingLanguage;
        this.stemming = da.stemming;
    }

    /**
     * Processes the input text and returns the found entities.
     *
     * @param text Input text on which the dictionary matching is executed
     * @return
     */
    public List<Annotation> getEntities(String text) {
        long start, end;
        start = System.currentTimeMillis();

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

        // tokenize text
        ProcessedText processedText = tokenizeText(tokenizer, text);

        // if stemming language was set, perform stemming of the input text
        if (stemming) {
            stemText(processedText);
        }

        // perform the look-up
        List<Annotation> entities = findEntities(processedText);

        // eliminate overlapping entities
        eliminateOverlapping(entities);

        List<Annotation> entitiesToReturn = new ArrayList<>();
        for (Annotation e : entities) {
            if (!e.overlap) {
                entitiesToReturn.add(e);
            }
        }

        end = System.currentTimeMillis();

        System.out.println("Extracting entities from input text (" + entitiesToReturn.size() + ") ... done [" + Double.toString((double) (end - start) / 1000) + " sec] .");
        return entitiesToReturn;
    }

    /**
     * This function runs the Aho-Corasick string matching algorithm on the tokenized (and stemmed) text using the
     * search tree built from the dictionary.
     *
     * @param pt The tokenized text
     */
    private List<Annotation> findEntities(ProcessedText processedText) {
        List<Annotation> entities = new ArrayList<>();
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

                entity = processedText.findMatch(begin, end);
                entity.setTokenizedBegin(begin);
                entity.setTokenizedEnd(end);

                entity.uri = stemming ? processedDictionary.getURI(str) : originalDictionary.getURI(entity.label);

                if (entity.getUri() != null) {
                    concept = stemming ? processedDictionary.getConcept(str) : originalDictionary.getConcept(entity.label);

                    if (concept.isPrefLabel()) {
                        entity.prefLabel = concept.labelText;
                        entity.altLabel = null;
                    } else {
                        entity.prefLabel = dictionary.getPrefLabel(entity.getUri());
                        entity.altLabel = concept.labelText;
                    }

                    entity.type = concept.type;

                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    /**
     * Tokenizes the original text and returns the tokenized text. If caseSensitive is true and caseSensitiveLength > 0
     * all tokens whose length is equal or bigger than the caseSensitiveLength are converted to lowercase. If
     * caseSensitive is true and caseSensitiveLength = 0 no conversion is applied. If caseSensitive is false all tokens
     * are converted to lowercase.
     *
     * @param tokenizer
     * @param text
     * @return
     */
    public ProcessedText tokenizeText(Tokenizer tokenizer, String text) {
        Span[] spans;
        StringBuilder sb;

        ProcessedText processedText = new ProcessedText(text);

        spans = tokenizer.tokenizePos(text);
        sb = new StringBuilder();

        Token t;
        String word;
        int position = 0;
        int begin, end;

        sb.append(" ");

        for (Span span : spans) {
            word = text.substring(span.getStart(), span.getEnd());
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

            processedText.addToken(t);

            sb.append(word);
            sb.append(" ");
        }
        processedText.setTokenizedText(sb.toString());
        return processedText;
    }

    /**
     * The function is responsible for the stemming of the main text based on the stemming language defined in the
     * constructor.
     */
    private void stemText(ProcessedText processedText) {
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
            throw new RuntimeException(e);
        }
    }

    /**
     * Eliminates the overlaps among all the entities found in the text. If we have two entities, Entity1 and Entity2,
     * and Entity1 is within the boundaries of Entity2, then Entity1 is marked and is later discarded. If the variable
     * eliminateOverlapping is true, entities whose boundaries are overlapping are also marked and are later discarded.
     */
    public void eliminateOverlapping(List<Annotation> entities) {
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
}
