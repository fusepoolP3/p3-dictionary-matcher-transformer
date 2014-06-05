/*
 * Copyright 2014 Reto.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.fusepool.sample.extractor;

import eu.fusepool.extractor.sample.LongRunningExtractor;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import eu.fusepool.extractor.sample.SimpleAsyncExtractor;
import eu.fusepool.extractor.server.ExtractorServer;
import java.net.ServerSocket;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Reto
 */
public class AsyncExtractorRestTest {

    @Before
    public void setUp() throws Exception {
        final int port = findFreePort();
        RestAssured.baseURI = "http://localhost:"+port+"/";
        ExtractorServer server = new ExtractorServer(port);
        server.start(new SimpleAsyncExtractor());
    }

    @Test
    public void turtleOnGet() {
        //Nothing Async-Specvific here
        Response response = RestAssured.given().header("Accept", "text/turtle")
                .expect().statusCode(HttpStatus.SC_OK).header("Content-Type", "text/turtle").when()
                .get();
    }
    
    @Test
    public void turtlePost() {
        Response response = RestAssured.given().header("Accept", "text/turtle")
                .contentType("text/plain;charset=UTF-8")
                .content("hello")
                .expect().statusCode(HttpStatus.SC_ACCEPTED).when()
                .post();
        String location = response.getHeader("location");
        Assert.assertNotNull("No location header in ACCEPTED- response", location);
        //we assume the next request is perfomed before the task finished
        Response response2 = RestAssured.given().header("Accept", "text/turtle")
                .expect().statusCode(HttpStatus.SC_ACCEPTED)
                .header("Content-Type", "text/turtle").when()
                .get(location);
        int count = 0;
        while (response2.getStatusCode() == HttpStatus.SC_ACCEPTED) {
            response2 = RestAssured.given().header("Accept", "text/turtle")
                .expect()
                .when()
                .get(location);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if (count++ > 65) {
                throw new RuntimeException("Async job not ending");
            }
        }
        Assert.assertEquals("Didn't get a 200 response eventually", HttpStatus.SC_OK, response2.getStatusCode());
        Assert.assertTrue("Result doesn't contain originally posted text", response2.getBody().asString().contains("hello"));
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
