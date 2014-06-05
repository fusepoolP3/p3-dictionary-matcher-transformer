/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.fusepool.extractor;

import eu.fusepool.extractor.util.WritingEntity;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;


public abstract class RdfGeneratingExtractor implements SyncExtractor {

    @Override
    public Set<MimeType> getSupportedOutputFormats() {
        final Set<String> supportedFormats = Serializer.getInstance().getSupportedFormats();
        final Set<MimeType> result = new HashSet<MimeType>();
        for (String string : supportedFormats) {
            try {
                result.add(new MimeType(string));
            } catch (MimeTypeParseException ex) {
                throw new RuntimeException(ex);
            }
        }
        return result;
    }
    
    public Entity extract(HttpRequestEntity entity) throws IOException {
        //TODO check content type matches supportedInputFormat
        //TODO content negotiation
        final String responseFormat = SupportedFormat.TURTLE;
        final TripleCollection generatedRdf = generateRdf(entity);
        return new WritingEntity() {

            @Override
            public MimeType getType() {
                try {
                    return new MimeType(responseFormat);
                } catch (MimeTypeParseException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void writeData(OutputStream out) throws IOException {    
                Serializer.getInstance().serialize(out, generatedRdf, responseFormat);
                out.flush();
            }
        };
        
        
        //baseRequest.setHandled(true);
        
    }

    protected abstract TripleCollection generateRdf(Entity entity) throws IOException;
    
}
