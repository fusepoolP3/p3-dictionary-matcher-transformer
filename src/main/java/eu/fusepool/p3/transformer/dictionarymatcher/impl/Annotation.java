package eu.fusepool.p3.transformer.dictionarymatcher.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class represents an entity and stores its label, URI, begin and end position, its weight, whether it overlaps
 * with other entities and the label divided into tokens.
 *
 * @author Gábor Reményi
 */
public class Annotation {

    String label;
    String prefLabel;
    String altLabel;
    String uri;
    String type;
    String beforeText;
    String afterText;
    private int begin;
    private int end;
    private int tokenizedBegin;
    private int tokenizedEnd;
    boolean overlap;
    Date timestamp;
    List<Token> tokens;

    /**
     * Simple constructor.
     */
    public Annotation() {
        timestamp = new Date();
        tokens = new ArrayList<>();
    }

    /**
     * Simple constructor.
     *
     * @param label
     * @param uri
     */
    public Annotation(String label, String uri) {
        this.label = label;
        this.uri = uri;
        timestamp = new Date();
        tokens = new ArrayList<>();
    }

    /**
     * Simple constructor.
     *
     * @param label
     * @param uri
     * @param type
     */
    public Annotation(String label, String uri, String type) {
        this.label = label;
        this.uri = uri;
        this.type = type;
        timestamp = new Date();
        tokens = new ArrayList<>();
    }

    /**
     * Returns the label of the entity.
     *
     * @return
     */
    public String getLabel() {
        return label;
    }

    public String getUri() {
        return uri;
    }

    public String getType() {
        return type;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public String getAltLabel() {
        return altLabel;
    }

    /**
     * Returns the label of the entity stripping it from new line characters.
     *
     * @return
     */
    public String getDisplayText() {
        return label.replace("\\n", " ");
    }

    /**
     * Returns the begin position of the entity.
     *
     * @return
     */
    public int getBegin() {
        return begin;
    }

    /**
     * Returns the end position of the entity.
     *
     * @return
     */
    public int getEnd() {
        return end;
    }

    /**
     * Returns the whether the entity overlaps with another.
     *
     * @return
     */
    public boolean isOverlap() {
        return overlap;
    }

    /**
     * Returns the label of the entity as a token list.
     *
     * @return
     */
    public List<Token> getTokens() {
        return tokens;
    }

    /**
     * Returns the length of the entity label.
     *
     * @return
     */
    public int getLength() {
        return end - begin;
    }

    /**
     * Adds a new token the to token list.
     *
     * @param t
     */
    public void addToken(Token t) {
        this.tokens.add(t);
    }

    public int getTokenizedBegin() {
        return tokenizedBegin;
    }

    public void setTokenizedBegin(int tokenizedBegin) {
        this.tokenizedBegin = tokenizedBegin;
    }

    public int getTokenizedEnd() {
        return tokenizedEnd;
    }

    public void setTokenizedEnd(int tokenizedEnd) {
        this.tokenizedEnd = tokenizedEnd;
    }

    public String getTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(timestamp);
    }

    /**
     * It is a lookup function to find the entity in the original text.
     *
     * @param originalText
     */
    public void findEntityInOriginalText(String originalText) {
        int tokenCount = this.tokens.size();

        if (tokenCount == 1) {
            begin = this.tokens.get(0).originalBegin;
            end = this.tokens.get(0).originalEnd;
            label = originalText.substring(begin, end);
        } else if (tokenCount > 1) {
            begin = this.tokens.get(0).originalBegin;
            end = this.tokens.get(tokenCount - 1).originalEnd;
            label = originalText.substring(begin, end);
        } else {
            label = "";
        }
    }

    @Override
    public String toString() {
        return "Entity{\r\n"
                + "\tprefLabel=\"" + prefLabel + "\",\r\n"
                + "\taltLabel=\"" + altLabel + "\",\r\n"
                + "\turi=\"" + uri + "\",\r\n"
                + "\ttype=\"" + type + "\",\r\n"
                + "\ttextFound=\"" + label + "\",\r\n"
                + "\tbegin=" + begin + ",\r\n"
                + "\tend=" + end + "\r\n"
                + "}\r\n";
    }
}
