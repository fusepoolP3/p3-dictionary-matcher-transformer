/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepoolp3.dictionarymatcher;

import eu.fusepool.extractor.Entity;
import eu.fusepool.extractor.RdfGeneratingExtractor;
import eu.fusepoolp3.datastore.Taxonomy;
import eu.fusepoolp3.dmasimple.Annotation;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
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
    protected TripleCollection generateRdf(Entity entity) throws IOException {
        final String data = IOUtils.toString(entity.getData(), "UTF-8");
        final TripleCollection result = new SimpleMGraph();  
        final GraphNode node = new GraphNode(new BNode(), result);
        
       System.out.println(data);
        
        String[] params = data.split("&");
        for (int i = 0; i < params.length; i++) {
            if(params[i].contains("=")){
                String[] temp = params[i].split("=", 2);
                params[i] = URLDecoder.decode(temp[1], "UTF-8");
            }
            else{
                params[i] = URLDecoder.decode(params[i], "UTF-8");
            }
//            System.out.println(params[i]);
        }
        
        DictionaryMatcher dm = DictionaryMatcher.getInstance();
        
        final String method = params[0];
        String url, text;
        GraphNode nodes;
        
        switch (method) {
            case "GetLabels":
                url = params[1];
                text = params[2];
                node.addProperty(RDF.type, new UriRef("http://example.org/ontology#TextDescription"));
                node.addPropertyValue(SIOC.content, text);
                node.addPropertyValue(new UriRef("http://example.org/ontology#textLength"), text.length());

                for (Annotation e : dm.GetLabels(url, text)) {
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
                break;
            case "GetTaxonomies":
                for (Taxonomy t : dm.GetTaxonomies()) {
                    nodes = new GraphNode(new BNode(), result);
                    nodes.addProperty(RDF.type, new UriRef("http://example.org/ontology#Taxonomy"));
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#id"), t.getID());
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#name"), t.getName());
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#reference"), new UriRef(t.getUri()));
                }
                break;
            case "AddTaxonomy":
                url = params[1];
                text = params[2];
                dm.AddTaxonomy(url, text);
                for (Taxonomy t : dm.GetTaxonomies()) {
                    nodes = new GraphNode(new BNode(), result);
                    nodes.addProperty(RDF.type, new UriRef("http://example.org/ontology#Taxonomy"));
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#id"), t.getID());
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#name"), t.getName());
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#reference"), new UriRef(t.getUri()));
                }
                break;
            case "DeleteTaxonomy":
                url = params[1];
                dm.DeleteTaxonomy(url);
                for (Taxonomy t : dm.GetTaxonomies()) {
                    nodes = new GraphNode(new BNode(), result);
                    nodes.addProperty(RDF.type, new UriRef("http://example.org/ontology#Taxonomy"));
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#id"), t.getID());
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#name"), t.getName());
                    nodes.addPropertyValue(new UriRef("http://example.org/ontology#reference"), new UriRef(t.getUri()));
                }
                break;
            default:
                throw new RuntimeException("Unknown method");
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
