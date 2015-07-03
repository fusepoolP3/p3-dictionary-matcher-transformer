package eu.fusepool.p3.transformer.dictionarymatcher;

import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.RdfGeneratingTransformer;
import eu.fusepool.p3.transformer.TransformerException;
import eu.fusepool.p3.transformer.dictionarymatcher.cache.Cache;
import eu.fusepool.p3.transformer.dictionarymatcher.impl.Annotation;
import eu.fusepool.p3.transformer.dictionarymatcher.impl.DictionaryAnnotator;
import eu.fusepool.p3.transformer.dictionarymatcher.impl.DictionaryStore;
import eu.fusepool.p3.transformer.dictionarymatcher.impl.Extractor;
import eu.fusepool.p3.transformer.dictionarymatcher.impl.Reader;
import eu.fusepool.p3.vocab.FAM;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
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

    final private static Object globalLock = new Object();
    private Map<String, String> queryParams;

    private DictionaryAnnotator dictionaryAnnotator;

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
            queryParams = Utils.getQueryParams(queryString);
        } catch (ArrayIndexOutOfBoundsException | UnsupportedEncodingException e) {
            throw new TransformerException(HttpServletResponse.SC_BAD_REQUEST, "ERROR: Badly formatted query string: \"" + queryString + "\" \nUsage: http://<transformer>/?taxonomy=<taxonomy_URI>");
        }

        // query params must not be empty
        if (queryParams.isEmpty()) {
            throw new TransformerException(HttpServletResponse.SC_BAD_REQUEST, "ERROR: Badly formatted query string: \"" + queryString + "\" \nUsage: http://<transformer>/?taxonomy=<taxonomy_URI>");
        }

        // get taxonomy URI
        final String taxonomy = queryParams.get("taxonomy");

        if (StringUtils.isBlank(taxonomy)) {
            throw new TransformerException(HttpServletResponse.SC_BAD_REQUEST, "ERROR: Taxonomy URI was not provided! \nUsage: http://<transformer>/?taxonomy=<taxonomy_URI>");
        }

        boolean caseSensitivity = queryParams.get("casesensitive") != null;

        // get stemming language
        String stemmingLanguage = queryParams.get("stemming");

        DictionaryStore dictionaryStore = null;
        InputStream inputStream;
        Object lock;
        long start, end;
        String cache = "";

        synchronized (globalLock) {
            lock = Cache.register(taxonomy);
        }

        synchronized (lock) {
            start = System.currentTimeMillis();
            // if not in the cache read it from provided uri
            if (!Cache.containsTaxonomy(taxonomy)) {
                try {
                    URI uri;
                    // see if url is valid
                    if (Utils.isURLValid(taxonomy)) {
                        uri = new URI(taxonomy);
                    } else {
                        // if it is not valid try to get it from resources
                        uri = Reader.class.getResource("/" + taxonomy).toURI();
                    }
                    URLConnection connection = uri.toURL().openConnection();
                    connection.setRequestProperty("Accept", "application/rdf+xml");
                    inputStream = connection.getInputStream();

                    // get the dictionary from reading the SKOS file
                    dictionaryStore = Reader.readDictionary(inputStream);

                    // process taxonomy
                    dictionaryAnnotator = new DictionaryAnnotator(dictionaryStore, stemmingLanguage, caseSensitivity, 0);

                    // add it to cache
                    Cache.setTaxonomy(taxonomy, dictionaryAnnotator);

                } catch (URISyntaxException | NullPointerException | IOException e) {
                    throw new TransformerException(HttpServletResponse.SC_BAD_REQUEST, "ERROR: Taxonomy URI is invalid! (\"" + taxonomy + "\")");
                }
            } else {
                cache = "(CACHED) ";
                // get it from cache
                dictionaryAnnotator = Cache.getTaxonomy(taxonomy);
            }
            end = System.currentTimeMillis();
            System.out.println("Loading " + cache + "taxonomy from " + taxonomy + " (" + dictionaryAnnotator.dictionary.getSize() + ") and creating transformer ... done [" + Double.toString((double) (end - start) / 1000) + " sec] .");
        }
    }

    @Override
    protected TripleCollection generateRdf(HttpRequestEntity entity) throws IOException {
        // get mimetype of content
        final MimeType mimeType = entity.getType();
        // get document URI
        final String docuentURI = Utils.getDocuementURI(entity);

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
            // create extractor instance
            Extractor extractor = new Extractor(dictionaryAnnotator);
            // create output from annotations
            for (Annotation e : extractor.getEntities(data)) {
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
    public Set<MimeType> getSupportedOutputFormats() {
        try {
            Set<MimeType> mimeTypes = new HashSet<>();
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
}
