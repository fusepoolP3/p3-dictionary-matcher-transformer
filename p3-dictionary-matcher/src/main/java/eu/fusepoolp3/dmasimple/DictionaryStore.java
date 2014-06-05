package eu.fusepoolp3.dmasimple;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the dictionary using a HashMap to store the
 * entity - URI pairs for fast retrieval.
 * @author Gábor Reményi
 */
public class DictionaryStore {
    // HashMap representation of the keywords and matching URIs
    Map<String, Concept> keywords;
    Map<String, String> prefLabels;
    
    /**
     * Simple constructor.
     */
    public DictionaryStore() {
        keywords = new HashMap<>();
        prefLabels = new HashMap<>();
    }
    
    /**
     * Add new element to the dictionary, label is transformed to lower case.
     * @param text  The label
     * @param uri   The URI
     */
    public void AddElement(String label, Concept concept){
        //concept.labelText = concept.labelText.toLowerCase();
        keywords.put(label.toLowerCase(), concept);
    }
    
    /**
     * Add new element to the dictionary without any change.
     * @param text  The label
     * @param uri   The URI
     */
    public void AddOriginalElement(String labelText, String labelType, String uri){
        Concept concept;
        
        if(keywords.containsKey(labelText)){
            concept = keywords.get(labelText);
        }
        else{
            concept = new Concept(labelText, labelType, uri);
        }
        
        keywords.put(labelText, concept);
        
        if(concept.IsPrefLabel()){
            prefLabels.put(uri, labelText);
        }
    }
    
    /**
     * Add new element to the dictionary.
     */
    public void AddOriginalElement(String labelText, String labelType, String uri, String type){
        Concept concept;

        if(keywords.containsKey(labelText)){
            concept = keywords.get(labelText);
        }
        else{
            concept = new Concept(labelText, labelType, uri, type);
        }
        
        keywords.put(labelText, concept);
        
        if(concept.IsPrefLabel()){
            prefLabels.put(uri, labelText);
        }
    }
    
    /**
     * Get the URI of the matching entity, label is transformed to lower case.
     * @param text The label
     * @return 
     */
    public String GetURI(String label){
        return keywords.get(label.toLowerCase()).uri.toString();
    }
    
    /**
     * Get the URI of the matching entity.
     * @param text The label
     * @return 
     */
    public String GetOriginalURI(String label){
        return keywords.get(label.toLowerCase()).uri.toString();
    }
        
    /**
     * Get the type of the matching entity.
     */
    public String GetType(String label){
        return keywords.get(label).type;
    }
    
    /**
     * Get concept object
     */   
    public Concept GetConcept(String labelText){
        return keywords.get(labelText);
    }
    
    /**
     * Get prefLabel by URI
     */   
    public String GetPrefLabel(String uri){
        return prefLabels.get(uri);
    }

    @Override
    public String toString() {
        String result = "";
        
        Set set = keywords.entrySet();
        Iterator iterator = set.iterator();
        
        int index = 0;
        String label;
        Concept concept;
        while(iterator.hasNext()){
            Map.Entry me = (Map.Entry)iterator.next();
            label = (String) me.getKey();
            concept = (Concept) me.getValue(); 
            if(concept != null){
                result += "\t\"" + label + "\", " + concept.toString() + "\r\n";
            }
            else{
                result += "\t\"" + label + "\", " + null + "\r\n";
            }
            index++;
        }         
        
        return "DictionaryStore{\r\n" + result + '}';
    }
    
    
}
