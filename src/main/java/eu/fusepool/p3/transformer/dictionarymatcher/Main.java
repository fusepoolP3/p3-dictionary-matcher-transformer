package eu.fusepool.p3.transformer.dictionarymatcher;

import eu.fusepool.p3.transformer.Transformer;
import eu.fusepool.p3.transformer.TransformerException;
import eu.fusepool.p3.transformer.TransformerFactory;
import eu.fusepool.p3.transformer.server.TransformerServer;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        TransformerServer server = new TransformerServer(arguments.getPort(), arguments.enableCors());

        // Map for caching transformers based on the query string
        final Map<String, DictionaryMatcherTransformer> transformers = new HashMap<>();

        server.start(
                new TransformerFactory() {
                    @Override
                    public Transformer getTransformer(HttpServletRequest request) {
                        switch (request.getMethod()) {
                            case "GET":
                                return new DictionaryMatcherTransformer();
                            case "POST":
                                DictionaryMatcherTransformer dictionaryMatcherTransformer = transformers.get(request.getQueryString());
                                // if transformer is not found in cache
                                if (dictionaryMatcherTransformer == null) {
                                    // create a new transformer
                                    dictionaryMatcherTransformer = new DictionaryMatcherTransformer(request.getQueryString());
                                    // put the transformer in the cache
                                    transformers.put(request.getQueryString(), dictionaryMatcherTransformer);
                                }
                                return dictionaryMatcherTransformer;
                            default:
                                throw new TransformerException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "ERROR: Method \"" + request.getMethod() + "\" is not allowed!");
                        }
                    }
                });

        server.join();
    }
}
