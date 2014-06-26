/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepoolp3.dictionarymatcher;

import eu.fusepoolp3.datastore.DataStore;
import eu.fusepoolp3.datastore.Taxonomy;
import eu.fusepoolp3.dmasimple.DictionaryAnnotator;
import eu.fusepoolp3.dmasimple.DictionaryStore;
import eu.fusepoolp3.dmasimple.Annotation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

/**
 * DictionaryMatcher
 * @author Gábor Reményi
 */
public class DictionaryMatcher {
    private HashMap<String, DictionaryAnnotator> extractors;
    private DictionaryStore dictionary;
    private DataStore datastore;
    
    private static DictionaryMatcher instance = null;
    
    private DictionaryMatcher() {
        System.out.println("Start reading taxonomies...");
        
        extractors = new HashMap<>();
        datastore = new DataStore();
        
        for (Taxonomy t : datastore.GetTaxonomies()) {
            System.out.println("Create dictionary matcher from taxonomy: " + t.getUri());
            CreateExtractor(t.getUri());
        }
    }
    
    public static DictionaryMatcher getInstance() {
        if (instance == null) {
            instance = new DictionaryMatcher();
        }
        return instance;
    }
    
    public List<Annotation> GetLabels(String uri, String text){
        DictionaryAnnotator extractor = extractors.get(uri);
        if(extractor != null){
            return extractor.GetEntities(text);
        }
        return null;
    }
    
    public List<Taxonomy> GetTaxonomies() {
        return datastore.GetTaxonomies();
    }

    public void AddTaxonomy(String url, String text) {
        if(datastore.AddTaxonomy(new Taxonomy(text, url))){
            if (!extractors.containsKey(url)) {
                CreateExtractor(url);
            }
        }
    }

    public void DeleteTaxonomy(String url) {
        if(datastore.DeleteTaxonomy(url)){
            if (extractors.containsKey(url)) {
                RemoveExtractor(url);
            }
        }
    }
    
    private void CreateExtractor(String url){
        try {
            URI uri = new URI(url);

            // get the dictionary from reading the SKOS file
            dictionary = ReadSKOS.GetDictionary(uri);

            // create the dictionary annotator instance
            extractors.put(url, new DictionaryAnnotator(dictionary, "English", false, 0, false));

        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void RemoveExtractor(String url){
        extractors.remove(url);
    }
    
    private String GetAbsolutePath(String relativePath){
        String absolultPath = new File("").getAbsolutePath() + relativePath;
        return absolultPath.replace("\\", "/");
    }
    
    private String ReadSampleText() throws IOException{
        try (BufferedReader br = new BufferedReader(new FileReader("data/test.txt"))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        }
    }
}
