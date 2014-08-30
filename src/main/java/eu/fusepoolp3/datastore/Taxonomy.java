/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepoolp3.datastore;

/**
 *
 * @author Gabor
 */
public class Taxonomy {
    private int id;
    private String name;
    private String uri;

    public Taxonomy(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }
    
    public Taxonomy(int id, String name, String uri) {
        this.id = id;
        this.name = name;
        this.uri = uri;
    }

    public String getID() {
        return Integer.toString(id);
    }
    
    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

}
