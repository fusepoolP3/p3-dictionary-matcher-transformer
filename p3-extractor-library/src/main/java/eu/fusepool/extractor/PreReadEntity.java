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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.MimeType;

/**
 * As the InputStream for a Request that has 
 * already returned is no longer available from the HttpRequest this class allows
 * to pre-read (cache) an entity.
 * 
 * @author reto
 */
//TODO check if other fields of HttpRequest, namely MediaType are still accessible
public class PreReadEntity extends HttpRequestEntity {
    private final HttpRequestEntity wrapped;
    private final byte[] data;

    public PreReadEntity(HttpRequestEntity wrapped) throws IOException {
        super(wrapped.getRequest());
        this.wrapped = wrapped;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wrapped.writeData(baos);
        this.data = baos.toByteArray();
    }

    @Override
    public MimeType getType() {
        return wrapped.getType();
    }

    @Override
    public InputStream getData() throws IOException {
        return new ByteArrayInputStream(data);
    }


}
