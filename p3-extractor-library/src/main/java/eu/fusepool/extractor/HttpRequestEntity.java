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

import eu.fusepool.extractor.util.InputStreamEntity;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author reto
 */
public class HttpRequestEntity extends InputStreamEntity {

    private final HttpServletRequest request;

    public HttpRequestEntity(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public MimeType getType() {
        String requestCt = request.getContentType();
        try {
            if (requestCt == null) {
                return new MimeType("application/octet-stream");
            } else {
                return new MimeType(requestCt);
            }
        } catch (MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public InputStream getData() throws IOException {
        return request.getInputStream();
    }

    /**
     * 
     * @return the underlying Servlet Request, need e.g. for content negotiation
     */
    public HttpServletRequest getRequest() {
        return request;
    }
    
    

}
