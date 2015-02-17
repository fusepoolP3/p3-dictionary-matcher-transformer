package eu.fusepool.p3.dictionarymatcher;

import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.SKOS04;

/**
 * This object represents a single triple in a SKOS taxonomy
 *
 * @author Gábor Reményi
 */
public class Concept {

    public String labelText;
    public UriRef labelType;
    public String uri;
    public String type;

    public Concept() {
    }

    public Concept(String labelText, UriRef labelType, String uri) {
        this.labelText = labelText;
        this.labelType = labelType;
        this.uri = uri;
    }

    public Concept(String labelText, UriRef labelType, String uri, String type) {
        this.labelText = labelText;
        this.labelType = labelType;
        this.uri = uri;
        this.type = type;
    }

    public Boolean isPrefLabel() {
        return SKOS04.prefLabel.equals(labelType);
    }

    public Boolean isAltLabel() {
        return SKOS04.altLabel.equals(labelType);
    }

    @Override
    public String toString() {
        return "Concept{" + "labelText=" + labelText + ", labelType=" + labelType + ", uri=" + uri + ", type=" + type + '}';
    }

}
