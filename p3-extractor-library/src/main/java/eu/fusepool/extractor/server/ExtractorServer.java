/*
 * Copyright 2014 Bern University of Applied Sciences.
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
package eu.fusepool.extractor.server;

import eu.fusepool.extractor.*;
import eu.fusepool.extractor.ExtractorHandlerFactory;
import org.eclipse.jetty.server.Server;

/**
 *
 * @author reto
 */
public class ExtractorServer {

    private final Server server;

    public ExtractorServer(int port) {
        server = new Server(port);
    }
    
    /**
     * 
     * @param extractor
     * @throws Exception ugly, but so does the underlying Jetty Server
     */
    public void start(Extractor extractor) throws Exception {
        server.setHandler(ExtractorHandlerFactory.getExtractorHandler(extractor));
        server.start();
        
    }
    
    public void join() throws InterruptedException {
        server.join();
    }
}
