package eu.fusepool.p3.transformer.dictionarymatcher.tests;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import eu.fusepool.p3.transformer.Transformer;
import eu.fusepool.p3.transformer.TransformerFactory;
import eu.fusepool.p3.transformer.dictionarymatcher.DictionaryMatcherTransformer;
import eu.fusepool.p3.transformer.server.TransformerServer;
import eu.fusepool.p3.vocab.FAM;
import java.net.ServerSocket;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for Dictionary Matcher Transformer
 */
public class TransformerTest {

    final private String testTaxonomy = "NASA.subjects.skos.xml";

    final private String testText = "The International Space Station (ISS) combines NASA's Space Station Freedom "
            + "project with the Soviet/Russian Mir-2 station, the European Columbus station, and the Japanese KibĹŤ "
            + "laboratory module. NASA originally planned in the 1980s to develop Freedom alone, but US budget "
            + "constraints led to the merger of these projects into a single multi-national program in 1993, "
            + "managed by NASA, the Russian Federal Space Agency (RKA), the Japan Aerospace Exploration Agency (JAXA), "
            + "the European Space Agency (ESA), and the Canadian Space Agency (CSA). The station consists of pressurized "
            + "modules, external trusses, solar arrays and other components, which have been launched by Russian Proton "
            + "and Soyuz rockets, and the US Space Shuttles. It is currently being assembled in Low Earth Orbit. The on-orbit "
            + "assembly began in 1998, the completion of the US Orbital Segment occurred in 2011 and the completion of the "
            + "Russian Orbital Segment is expected by 2016. The ownership and use of the space station is established "
            + "in intergovernmental treaties and agreements which divide the station into two areas and allow Russia to "
            + "retain full ownership of the Russian Orbital Segment (with the exception of Zarya), with the US Orbital Segment "
            + "allocated between the other international partners.";

    private String baseURI;

    @Before
    public void setUp() throws Exception {
        final int port = findFreePort();
        baseURI = "http://localhost:" + port + "/";
        TransformerServer server = new TransformerServer(port, false);
        server.start(new TransformerFactory() {
            @Override
            public Transformer getTransformer(HttpServletRequest request) {
                if (StringUtils.isNotEmpty(request.getQueryString())) {
                    return new DictionaryMatcherTransformer(request.getQueryString());
                } else {
                    return new DictionaryMatcherTransformer();
                }
            }
        });
    }

    @Test
    public void turtleOnGet() {
        Response response = RestAssured.given().header("Accept", "text/turtle")
                .expect().statusCode(HttpStatus.SC_OK).header("Content-Type", "text/turtle").when()
                .get(baseURI);
    }

    @Test
    public void turtlePost() {
        Response response = RestAssured.given().header("Accept", "text/turtle")
                .contentType("text/plain;charset=UTF-8")
                .content(testText)
                .expect().statusCode(HttpStatus.SC_OK).header("Content-Type", "text/turtle").when()
                .post(baseURI + "?taxonomy=" + testTaxonomy);
        System.out.println(response.getBody().toString());
        Graph graph = Parser.getInstance().parse(response.getBody().asInputStream(), "text/turtle");
        Iterator<Triple> typeTriples = graph.filter(null, RDF.type, FAM.LinkedEntity);
        Assert.assertTrue("No type triple found", typeTriples.hasNext());
    }

    public static int findFreePort() {
        int port = 0;
        try (ServerSocket server = new ServerSocket(0);) {
            port = server.getLocalPort();
        } catch (Exception e) {
            throw new RuntimeException("unable to find a free port");
        }
        return port;
    }
}
