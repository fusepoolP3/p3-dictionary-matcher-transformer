/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepoolp3.dmasimple;

/**
 * This object represents a single triple in a SKOS taxonomy
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
    
    public Boolean IsPrefLabel(){
        if(LabelType.PREF.toString().equals(labelType.toString())){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Concept{" + "labelText=" + labelText + ", labelType=" + labelType + ", uri=" + uri + ", type=" + type + '}';
    }
    
    
}
