/*
 * Copyright 2014 reto.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.activation.MimeType;

/**
 *
 * @author reto
 */
class LongRunningExtractorWrapper implements AsyncExtractor {
    private CallBackHandler callBackHandler;
    private final SyncExtractor wrapped;
    private final Set<String> activeRequests = Collections.synchronizedSet(new HashSet<String>());

    public LongRunningExtractorWrapper(SyncExtractor wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void activate(CallBackHandler callBackHandler) {
        this.callBackHandler = callBackHandler;
    }

    @Override
    public void extract(final HttpRequestEntity requestEntity, final String requestId) throws IOException {
        activeRequests.add(requestId);
        final PreReadEntity preReadEntity = new PreReadEntity(requestEntity);
        (new Thread() {

            @Override
            public void run() {
                try {
                    callBackHandler.responseAvailable(requestId, wrapped.extract(preReadEntity));
                } catch (Exception ex) {
                    callBackHandler.reportException(requestId, ex);
                } 
                activeRequests.remove(requestId);
            }
            
        }).start();
        
    }

    @Override
    public boolean isActive(String requestId) {
        return activeRequests.contains(requestId);
    }

    @Override
    public Set<MimeType> getSupportedInputFormats() {
        return wrapped.getSupportedInputFormats();
    }

    @Override
    public Set<MimeType> getSupportedOutputFormats() {
        return wrapped.getSupportedOutputFormats();
    }
    
}
