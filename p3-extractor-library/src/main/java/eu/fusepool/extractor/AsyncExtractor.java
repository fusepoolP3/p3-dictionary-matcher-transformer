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
package eu.fusepool.extractor;

import java.io.IOException;


/**
 * For asynchronous extractor the response entity is provided to a CallBackHandler
 * (typically) after the extract method returned.
 * 
 * The reason why there is a single callBackHandler per extractor and not one
 * CallBackHandler per request (i.e. per invocation of the extract method) is to
 * allow extractions tasks to survive restarts of the server. For example when
 * an extraction is requested the extract method might send and email, a thread
 * checking the mailbox for an email answer might be started by the 
 * activate method. The email answer would contain the requestId allowing to map 
 * a received answer email to the original request.
 * 
 * @author reto
 */
public interface AsyncExtractor extends Extractor {

    public interface  CallBackHandler {
        abstract void responseAvailable(String requestId, Entity response);

        public void reportException(String requestId, Exception ex);
    }

    void activate(CallBackHandler callBackHandler);
    
    void extract(HttpRequestEntity entity, String requestId) throws IOException;
    
    /**
     * Checks if a requestId is being processed by the Extractor. The extractor
     * should return true if CallBackHandler.responseAvailable might be called
     * for the given requestId.
     * 
     * @param requestId the requestId
     * @return true if the extractor is processing a request, false otherwise.
     */
    boolean isActive(String requestId);


}
