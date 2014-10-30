package eu.fusepool.p3.dictionarymatcher;

/**
 * This object represents a single triple in a SKOS taxonomy
 *
 * @author Gábor Reményi
 */
public class Concept {

    public String labelText;
    public LabelType labelType;
    public String uri;
    public String type;

    public Concept() {
    }

    public Concept(String labelText, String labelType, String uri) {
        this.labelText = labelText;
        this.labelType = LabelType.getByValue(labelType);
        this.uri = uri;
    }

    public Concept(String labelText, String labelType, String uri, String type) {
        this.labelText = labelText;
        this.labelType = LabelType.getByValue(labelType);
        this.uri = uri;
        this.type = type;
    }

    public Boolean isPrefLabel() {
        return LabelType.isPrefLabel(labelType);
    }

    public Boolean isAltLabel() {
        return LabelType.isAltLabel(labelType);
    }

    @Override
    public String toString() {
        return "Concept{" + "labelText=" + labelText + ", labelType=" + labelType + ", uri=" + uri + ", type=" + type + '}';
    }

}
