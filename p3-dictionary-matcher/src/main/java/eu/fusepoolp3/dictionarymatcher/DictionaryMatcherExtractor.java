/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepoolp3.dictionarymatcher;

import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.RdfGeneratingExtractor;
import eu.fusepoolp3.datastore.Taxonomy;
import eu.fusepoolp3.dmasimple.Annotation;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.SIOC;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Gabor
 */
public class DictionaryMatcherExtractor extends RdfGeneratingExtractor {

    @Override
    protected TripleCollection generateRdf(HttpRequestEntity entity) throws IOException {
        final String queryString = entity.getRequest().getQueryString();
        final String data = IOUtils.toString(entity.getData(), "UTF-8");
        final TripleCollection result = new SimpleMGraph();  
        final GraphNode node = new GraphNode(new BNode(), result);
        GraphNode nodes;
        
//        System.out.println(queryString);
//        System.out.println(data);
       
        HashMap<String,String> queryParams = new HashMap<>();
        
        if(queryString != null){
            String[] params = queryString.split("&");
            String[] param;
            for (int i = 0; i < params.length; i++) {
                param = params[i].split("=", 2);
                queryParams.put(param[0], param[1]);
            }
        }
        
        DictionaryMatcher dm = DictionaryMatcher.getInstance();
        
        String urldecode = queryParams.get("urldecode");
        String uri = queryParams.get("taxonomy");
        String name = queryParams.get("name");
        String delete = queryParams.get("delete");
        String text = data;
        
        if(urldecode != null){
            if(uri != null) { uri = URLDecoder.decode(uri, "UTF-8"); }
            if(name != null) { name = URLDecoder.decode(name, "UTF-8"); }
            if(text != null) { text = URLDecoder.decode(data, "UTF-8"); }
        }  
        
        if(uri != null){
            // if no name was provided add default (for gui purpose)
            if(name == null) { name = uri; }
            
            // if uri of the taxonomy and data were provided annotate text
            if (text != null && !text.isEmpty()) {
//                node.addProperty(RDF.type, new UriRef("http://example.org/ontology#TextDescription"));
//                node.addPropertyValue(SIOC.content, text);
//                node.addPropertyValue(new UriRef("http://example.org/ontology#textLength"), text.length());

                // if taxonomy does not exist add it first
                if(!dm.IsExisting(uri)){
                    dm.AddTaxonomy(uri, name);
                }
                
                // create output from annotations
                for (Annotation e : dm.GetLabels(uri, text)) {
                    nodes = new GraphNode(new BNode(), result);
                    nodes.addProperty(RDF.type, new UriRef("http://example.org/ontology#Annotation"));
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#prefLabel"), e.getPrefLabel());
                    if (e.getAltLabel() != null) {
                        nodes.addPropertyValue(new UriRef("http://example.org/ontology#altLabel"), e.getAltLabel());
                    }
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#reference"), new UriRef(e.getUri()));
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#textFound"), e.getLabel());
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#begin"), e.getBegin());
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#end"), e.getEnd());
                }
            } else {
                // if only the taxonomy was provided add as new and return existing taxonomies
                if(delete == null){
                    if(!dm.IsExisting(uri)){
                        dm.AddTaxonomy(uri, name);
                    }
                }
                // if only the taxonomy and a delete flag were provided delete given taxonomy and return remaining taxonomies
                else{
                    dm.DeleteTaxonomy(uri);
                }
                // create output from taxonomies
                for (Taxonomy t : dm.GetTaxonomies()) {
                    nodes = new GraphNode(new BNode(), result);
                    nodes.addProperty(RDF.type, new UriRef("http://example.org/ontology#Taxonomy"));
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#id"), t.getID());
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#name"), t.getName());
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#reference"), new UriRef(t.getUri()));
                }
            }
        }
        // if no uri string were provided get existing taxonomies
        else{ 
            // create output from taxonomies
            for (Taxonomy t : dm.GetTaxonomies()) {
                nodes = new GraphNode(new BNode(), result);
                nodes.addProperty(RDF.type, new UriRef("http://example.org/ontology#Taxonomy"));
                nodes.addPropertyValue(new UriRef("http://example.org/ontology#id"), t.getID());
                nodes.addPropertyValue(new UriRef("http://example.org/ontology#name"), t.getName());
                nodes.addPropertyValue(new UriRef("http://example.org/ontology#reference"), new UriRef(t.getUri()));
            }
        }
        
        return result;
    }


    @Override
    public Set<MimeType> getSupportedInputFormats() {
        try {
            MimeType mimeType = new MimeType("text/plain;charset=UTF-8");
            return Collections.singleton(mimeType);
        } catch (MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public boolean isLongRunning() {
        return false;
    }
}
