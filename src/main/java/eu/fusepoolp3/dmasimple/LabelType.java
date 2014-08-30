/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepoolp3.dmasimple;

/**
 *
 * @author Gabor
 */
public enum LabelType {
    PREF("prefLabel"),
    ALT("altLabel");
    
    private final String name;
        
    private LabelType(final String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public static LabelType getByName(String name) {
        return LabelType.valueOf(name);
    }
    
    public static LabelType getByValue(String value) {
        for (LabelType e : LabelType.values()) {
            if (e.name.equals(value)) {
                return e;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
