package eu.fusepool.p3.transformer.dictionarymatcher;

import eu.fusepool.p3.transformer.Transformer;
import eu.fusepool.p3.transformer.TransformerException;
import eu.fusepool.p3.transformer.TransformerFactory;
import eu.fusepool.p3.transformer.server.TransformerServer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
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

        // create the singleton instance of Serializer
        Serializer.getInstance();
        // create the singleton instance of Parser
        Parser.getInstance();

        server.start(
                new TransformerFactory() {
                    @Override
                    public Transformer getTransformer(HttpServletRequest request) {
                        switch (request.getMethod()) {
                            case "GET":
                                return new DictionaryMatcherTransformer();
                            case "POST":
                                return new DictionaryMatcherTransformer(request.getQueryString());
                            default:
                                throw new TransformerException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "ERROR: Method \"" + request.getMethod() + "\" is not allowed!");
                        }
                    }
                });

        server.join();
    }
}
