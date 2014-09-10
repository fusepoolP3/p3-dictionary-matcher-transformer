package eu.fusepoolp3.dictionarymatcher;

import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.RdfGeneratingTransformer;
import eu.fusepoolp3.dmasimple.Annotation;
import eu.fusepoolp3.dmasimple.DictionaryAnnotator;
import eu.fusepoolp3.dmasimple.DictionaryStore;
import eu.fusepoolp3.dmasimple.Skos;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Gabor
 */
public class DictionaryMatcherTransformer extends RdfGeneratingTransformer {

    private Map<String, String> queryParams;
    private DictionaryAnnotator dictionaryAnnotator;
    private DictionaryStore dictionary;
    
    /**
     * Default constructor for GET
     */
    public DictionaryMatcherTransformer() {
    
    }
    
    /**
     * Constructor for POST
     * 
     * @param queryString 
     */
    public DictionaryMatcherTransformer(String queryString) {
        // get query params from query string
        queryParams = getQueryParams(queryString);

        // query string must not be empty
        if (queryParams.isEmpty()) {
            throw new RuntimeException("Query string must not be empty!");
        }
        
        String taxonomy = queryParams.get("taxonomy");

        if (StringUtils.isEmpty(taxonomy)) {
            throw new RuntimeException("Taxonomy URI must not be empty!");
        }
        
        // create new dictionaryAnnotator if it does not exists
        if (dictionaryAnnotator == null) {
            long start, end;
            try {
                System.out.print("Loading taxonomy from " + taxonomy);
                start = System.currentTimeMillis();

                // get the dictionary from reading the SKOS file
                dictionary = Skos.ReadDictionary(new URI(taxonomy));

                System.out.print(" (" + dictionary.GetSize() + ") and creating transformer ...");

                // create the dictionary annotator instance
                dictionaryAnnotator = new DictionaryAnnotator(dictionary, "English", false, 0, false);      // TODO get settings from query string

                end = System.currentTimeMillis();
                System.out.println(" done [" + Double.toString((double) (end - start) / 1000) + " sec] .");

            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected TripleCollection generateRdf(HttpRequestEntity entity) throws IOException {
        // get text data from request
        final String data = IOUtils.toString(entity.getData(), "UTF-8");
        final TripleCollection result = new SimpleMGraph();
        final GraphNode node = new GraphNode(new BNode(), result);
        GraphNode nodes;

        node.addProperty(RDF.type, new UriRef("http://example.org/ontology#TextDescription"));
        node.addPropertyValue(SIOC.content, data);
        node.addPropertyValue(new UriRef("http://example.org/ontology#textLength"), data.length());

        // if data is empty or blank do not invoke the annotator
        if (StringUtils.isNotBlank(data)) {
            // create output from annotations
            for (Annotation e : dictionaryAnnotator.GetEntities(data)) {
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

    /**
     * Get query parameters from a query string.
     *
     * @param queryString the query string
     * @return HashMap containing the query parameters
     */
    private Map<String, String> getQueryParams(String queryString) {
        Map<String, String> temp = new HashMap<>();
        // query string should not be empty or blank
        if (StringUtils.isNotBlank(queryString)) {
            String[] params = queryString.split("&");
            String[] param;
            for (int i = 0; i < params.length; i++) {
                param = params[i].split("=", 2);
                temp.put(param[0], param[1]);
            }
        }
        return temp;
    }
}
