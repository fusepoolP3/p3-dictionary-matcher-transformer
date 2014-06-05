/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepoolp3.dictionarymatcher;

import eu.fusepool.extractor.Entity;
import eu.fusepool.extractor.RdfGeneratingExtractor;
import eu.fusepoolp3.dmasimple.Annotation;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.DC;
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
        final String text = IOUtils.toString(entity.getData(), "UTF-8");
        final TripleCollection result = new SimpleMGraph();  
        
        final GraphNode node = new GraphNode(new BNode(), result);
        node.addProperty(RDF.type, new UriRef("http://example.org/ontology#TextDescription"));
        node.addPropertyValue(SIOC.content, text);
        node.addPropertyValue(new UriRef("http://example.org/ontology#textLength"), text.length());
        
        DictionaryMatcher dm = DictionaryMatcher.getInstance();
        List<Annotation> enhancements = dm.GetEntities(text);
        
        GraphNode enhancementNode;
        for (Annotation e : enhancements) {
            enhancementNode = new GraphNode(new BNode(), result);
            enhancementNode.addProperty(RDF.type, new UriRef("http://example.org/ontology#Enhancement"));
            enhancementNode.addPropertyValue(new UriRef("http://example.org/ontology#prefLabel"), e.getPrefLabel());
            if(e.getAltLabel() != null){
                enhancementNode.addPropertyValue(new UriRef("http://example.org/ontology#altLabel"), e.getAltLabel());
            }
            enhancementNode.addPropertyValue(new UriRef("http://example.org/ontology#entityReference"), new UriRef(e.getUri()));
            enhancementNode.addPropertyValue(new UriRef("http://example.org/ontology#textFound"), e.getLabel());
            enhancementNode.addPropertyValue(new UriRef("http://example.org/ontology#begin"), e.getBegin());
            enhancementNode.addPropertyValue(new UriRef("http://example.org/ontology#end"), e.getEnd());
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
