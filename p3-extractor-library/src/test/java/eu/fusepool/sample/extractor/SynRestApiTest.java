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

import eu.fusepool.extractor.sample.SimpleExtractor;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import eu.fusepool.extractor.server.ExtractorServer;
import java.net.ServerSocket;
import org.apache.http.HttpStatus;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Reto
 */
public class SynRestApiTest {

    @Before
    public void setUp() throws Exception {
        final int port = findFreePort();
        RestAssured.baseURI = "http://localhost:"+port+"/";
        ExtractorServer server = new ExtractorServer(port);
        server.start(new SimpleExtractor());
    }

    @Test
    public void turtleOnGet() {
        Response response = RestAssured.given().header("Accept", "text/turtle")
                .expect().statusCode(HttpStatus.SC_OK).header("Content-Type", "text/turtle").when()
                .get();
    }
    
    @Test
    public void turtlePost() {
        Response response = RestAssured.given().header("Accept", "text/turtle")
                .contentType("text/plain;charset=UTF-8")
                .content("hello")
                .expect().statusCode(HttpStatus.SC_OK).content(new StringContains("hello")).header("Content-Type", "text/turtle").when()
                .post();
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
