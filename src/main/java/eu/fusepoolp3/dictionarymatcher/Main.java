package eu.fusepoolp3.dictionarymatcher;

import eu.fusepool.p3.transformer.Transformer;
import eu.fusepool.p3.transformer.TransformerFactory;
import eu.fusepool.p3.transformer.server.TransformerServer;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.wymiwyg.commons.util.arguments.ArgumentHandler;

public class Main {

    public static void main(String[] args) throws Exception {
        Arguments arguments = ArgumentHandler.readArguments(Arguments.class, args);
        if (arguments != null) {
            start(arguments);
        }
    }

    /**
     * Starts the transformer server.
     *
     * @param arguments contains the port on which the server will listen
     * @throws Exception
     */
    private static void start(Arguments arguments) throws Exception {
        TransformerServer server = new TransformerServer(arguments.getPort());

        // Map for caching transformers based on the query string
        final Map<String, DictionaryMatcherTransformer> transformers = new HashMap<>();

        server.start(
                new TransformerFactory() {
                    @Override
                    public Transformer getTransformer(HttpServletRequest request) {
                        if (StringUtils.isNotEmpty(request.getQueryString())) {
                            DictionaryMatcherTransformer dictionaryMatcherTransformer = transformers.get(request.getQueryString());
                            // if pipeline transformer is not found in cache
                            if (dictionaryMatcherTransformer == null) {
                                // create a new transformer
                                dictionaryMatcherTransformer = new DictionaryMatcherTransformer(request.getQueryString());
                                // put the pipeline transformer in the cache 
                                transformers.put(request.getQueryString(), dictionaryMatcherTransformer);
                            }
                            return dictionaryMatcherTransformer;
                        } else {
                            return new DictionaryMatcherTransformer();
                        }
                    }
                });

        server.join();
    }
}
