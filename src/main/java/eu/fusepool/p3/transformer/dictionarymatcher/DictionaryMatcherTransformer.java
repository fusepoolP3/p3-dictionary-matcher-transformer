package eu.fusepool.p3.transformer.dictionarymatcher;

import eu.fusepool.p3.dictionarymatcher.Annotation;
import eu.fusepool.p3.dictionarymatcher.DictionaryAnnotator;
import eu.fusepool.p3.dictionarymatcher.DictionaryStore;
import eu.fusepool.p3.dictionarymatcher.Skos;
import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.RdfGeneratingTransformer;
import eu.fusepool.p3.transformer.TransformerException;
import eu.fusepool.p3.vocab.FAM;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.serializedform.Parser;
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
     * Default constructor for GET.
     */
    public DictionaryMatcherTransformer() {
    }

    /**
     * Constructor for POST.
     *
     * @param queryString
     */
    public DictionaryMatcherTransformer(String queryString) {
        // query string must not be empty
        if (StringUtils.isEmpty(queryString)) {
            throw new TransformerException(HttpServletResponse.SC_BAD_REQUEST, "ERROR: Query string must not be emtpy! \nUsage: http://<transformer>/?taxonomy=<taxonomy_URI>");
        }

        // get query params from query string
        try {
            queryParams = getQueryParams(queryString);
        } catch (IndexOutOfBoundsException e) {
            throw new TransformerException(HttpServletResponse.SC_BAD_REQUEST, "ERROR: Badly formatted query string: \"" + queryString + "\" \nUsage: http://<transformer>/?taxonomy=<taxonomy_URI>");
        }

        // query params must not be empty
        if (queryParams.isEmpty()) {
            throw new TransformerException(HttpServletResponse.SC_BAD_REQUEST, "ERROR: Badly formatted query string: \"" + queryString + "\" \nUsage: http://<transformer>/?taxonomy=<taxonomy_URI>");
        }

        String taxonomy = queryParams.get("taxonomy");

        if (StringUtils.isEmpty(taxonomy)) {
            throw new TransformerException(HttpServletResponse.SC_BAD_REQUEST, "ERROR: Taxonomy URI was not provided! \nUsage: http://<transformer>/?taxonomy=<taxonomy_URI>");
        }

        // create new dictionaryAnnotator if it does not exists
        if (dictionaryAnnotator == null) {
            long start, end;
            System.out.print("Loading taxonomy from " + taxonomy);
            start = System.currentTimeMillis();

            // get the dictionary from reading the SKOS file
            dictionary = Skos.readDictionary(taxonomy);

            System.out.print(" (" + dictionary.GetSize() + ") and creating transformer ...");

            // create the dictionary annotator instance
            dictionaryAnnotator = new DictionaryAnnotator(dictionary, "English", false, 0, false);      // TODO get settings from query string

            end = System.currentTimeMillis();
            System.out.println(" done [" + Double.toString((double) (end - start) / 1000) + " sec] .");
        }
    }

    @Override
    protected TripleCollection generateRdf(HttpRequestEntity entity) throws IOException {
        // get mimetype of content
        final MimeType mimeType = entity.getType();

        // get document URI
        final String docuentURI = getDocuementURI(entity);

        String data = null;
        try {
            // handle data based on content type
            if (mimeType.match("text/turtle")) {
                Graph graph = Parser.getInstance().parse(entity.getData(), "text/turtle");
                Iterator<Triple> typeTriples = graph.filter(null, SIOC.content, null);
                if (!typeTriples.hasNext()) {
                    throw new TransformerException(HttpServletResponse.SC_BAD_REQUEST, "ERROR: No type triple found with predicate " + SIOC.content);
                }
                StringBuilder result = new StringBuilder();
                int count = 0;
                while (typeTriples.hasNext()) {
                    Literal literal = (Literal) typeTriples.next().getObject();
                    // if there is more than one triple separate them with new line
                    if (count > 0) {
                        result.append(System.getProperty("line.separator"));
                    }
                    result.append(literal.getLexicalForm());
                    count++;
                }
                data = result.toString();
            } else {
                // get text data from request
                data = IOUtils.toString(entity.getData(), "UTF-8");
            }
        } catch (MimeTypeParseException e) {
            throw new RuntimeException(e);
        }

        final TripleCollection result = new SimpleMGraph();
        GraphNode node;
        // if data is empty or blank do not invoke the annotator
        if (StringUtils.isNotBlank(data)) {
            int i = 1;
            // create output from annotations
            for (Annotation e : dictionaryAnnotator.GetEntities(data)) {
                // create selector URI
                String selector = docuentURI + "#char=" + e.getBegin() + "," + e.getEnd();
                // create annotation-body URI
                String annotationBody = docuentURI + "#annotation-body" + i;
                // create annotation URI
                String annotation = docuentURI + "#annotation" + i;
                // create sp-resource URI
                String spResource = docuentURI + "#sp-resource" + i;

                // Linked Entity Annotation (body)
                node = new GraphNode(new UriRef(annotationBody), result);
                node.addProperty(RDF.type, FAM.LinkedEntity);
                if (e.getAltLabel() != null) {
                    node.addPropertyValue(FAM.entity_label, e.getAltLabel());
                } else {
                    node.addPropertyValue(FAM.entity_label, e.getPrefLabel());
                }
                node.addProperty(FAM.entity_reference, new UriRef(e.getUri()));
                node.addPropertyValue(FAM.entity_mention, e.getLabel());
                node.addProperty(FAM.extracted_from, new UriRef(docuentURI));
                node.addProperty(FAM.selector, new UriRef(selector));

                // oa:Annotation
                node = new GraphNode(new UriRef(annotation), result);
                node.addProperty(RDF.type, new UriRef("http://www.w3.org/ns/oa#Annotation"));
                node.addProperty(new UriRef("http://www.w3.org/ns/oa#hasBody"), new UriRef(annotationBody));
                node.addProperty(new UriRef("http://www.w3.org/ns/oa#hasTarget"), new UriRef(spResource));
                node.addProperty(new UriRef("http://www.w3.org/ns/oa#annotatedBy"), new UriRef("p3-dictionary-matcher-transformer"));
                node.addPropertyValue(new UriRef("http://www.w3.org/ns/oa#annotatedAt"), e.getTimestamp());

                // oa:SpecificResource
                node = new GraphNode(new UriRef(spResource), result);
                node.addProperty(RDF.type, new UriRef("http://www.w3.org/ns/oa#SpecificResource"));
                node.addProperty(new UriRef("http://www.w3.org/ns/oa#hasSource"), new UriRef(annotationBody));
                node.addProperty(new UriRef("http://www.w3.org/ns/oa#hasSelector"), new UriRef(selector));

                // NIF selector
                node = new GraphNode(new UriRef(selector), result);
                node.addProperty(RDF.type, FAM.NifSelector);
                node.addProperty(RDF.type, new UriRef("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#String"));
                node.addPropertyValue(new UriRef("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#beginIndex"), e.getBegin());
                node.addPropertyValue(new UriRef("http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#endIndex"), e.getEnd());

                i++;
            }
        } else {
            throw new TransformerException(HttpServletResponse.SC_BAD_REQUEST, "ERROR: Input text was not provided!");
        }

        return result;
    }

    @Override
    public Set<MimeType> getSupportedInputFormats() {
        try {
            Set<MimeType> mimeTypes = new HashSet<>();
            mimeTypes.add(new MimeType("text/plain"));
            mimeTypes.add(new MimeType("text/turtle"));
            return mimeTypes;
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
    private Map<String, String> getQueryParams(String queryString) throws ArrayIndexOutOfBoundsException {
        Map<String, String> temp = new HashMap<>();
        // query string should not be empty or blank
        if (StringUtils.isNotBlank(queryString)) {
            String[] params = queryString.split("&");
            String[] param;
            for (String item : params) {
                param = item.split("=", 2);
                temp.put(param[0], param[1]);
            }
        }
        return temp;
    }

    /**
     * Get docuemt URI either from content location header, or generate one if
     * it's null.
     *
     * @param entity
     * @return
     */
    private String getDocuementURI(HttpRequestEntity entity) {
        String documentURI;

        if (entity.getContentLocation() == null) {
            HttpServletRequest request = entity.getRequest();
            String baseURL = getBaseURL(request);
            String requestID = request.getHeader("X-Request-ID");

            if (StringUtils.isNotEmpty(requestID)) {
                documentURI = baseURL + requestID;
            } else {
                documentURI = baseURL + UUID.randomUUID().toString();
            }
        } else {
            documentURI = entity.getContentLocation().toString();
        }

        return documentURI;
    }

    /**
     * Returns the base URL.
     *
     * @param request
     * @return
     */
    public static String getBaseURL(HttpServletRequest request) {
        if ((request.getServerPort() == 80) || (request.getServerPort() == 443)) {
            return request.getScheme() + "://" + request.getServerName() + "/";
        } else {
            return request.getScheme() + "://" + request.getServerName() + ":"
                    + request.getServerPort() + "/";
        }
    }

    /**
     * For testing purposes.
     *
     * @param request
     */
    public void printHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {

            String headerName = headerNames.nextElement();
            System.out.print(headerName);

            Enumeration<String> headers = request.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                String headerValue = headers.nextElement();
                System.out.println("\t" + headerValue);
            }
        }
    }
}
