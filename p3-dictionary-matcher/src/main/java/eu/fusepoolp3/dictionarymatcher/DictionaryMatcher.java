/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepoolp3.dictionarymatcher;

import eu.fusepoolp3.dmasimple.DictionaryAnnotator;
import eu.fusepoolp3.dmasimple.DictionaryStore;
import eu.fusepoolp3.dmasimple.Annotation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * DictionaryMatcher
 * @author Gábor Reményi
 */
public class DictionaryMatcher {
    
    // path of SKOS file 
    final private String filePath = "\\src\\main\\resources\\apitest.owl";
//    final private String filePath = "\\src\\main\\resources\\jurivoc_sample.nt";
//    final private String filePath = "\\src\\main\\resources\\jurivoc_all.nt";
    
    private DictionaryAnnotator da;
    private DictionaryStore dictionary;
    
    private static DictionaryMatcher instance = null;
    
    protected DictionaryMatcher() {
                
        System.out.println("Start reading SKOS file...");
        
        String absolultPath = GetAbsolutePath(filePath);
        
        // get the dictionary from reading the SKOS file
        dictionary = ReadSKOS.GetDictionary(absolultPath);

        // create the dictionary annotator instance
        da = new DictionaryAnnotator(dictionary, "English", false, 0, false);
    }
    
    public static DictionaryMatcher getInstance() {
        if (instance == null) {
            instance = new DictionaryMatcher();
        }
        return instance;
    }
    
    public List<Annotation> GetEntities(String text){
        return da.GetEntities(text);
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
