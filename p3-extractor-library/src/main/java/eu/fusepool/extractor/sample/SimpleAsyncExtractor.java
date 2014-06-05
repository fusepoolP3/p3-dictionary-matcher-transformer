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

package eu.fusepool.extractor.sample;

import eu.fusepool.extractor.AsyncExtractor;
import eu.fusepool.extractor.Entity;
import eu.fusepool.extractor.HttpRequestEntity;
import eu.fusepool.extractor.PreReadEntity;
import eu.fusepool.extractor.util.WritingEntity;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author reto
 */
public class SimpleAsyncExtractor implements AsyncExtractor {

    final Queue<ExtractorJob> pendingJobs = new ConcurrentLinkedQueue<>();// Collections.synchronizedSet(new HashSet<>());
    final Set<String> activeJobs = Collections.synchronizedSet(new HashSet<String>());
    private CallBackHandler callBackHandler;
    
    @Override
    public void activate(CallBackHandler callBackHandler) {
        this.callBackHandler = callBackHandler;
        Thread workerThread = new WorkerThread();
        workerThread.start();
    }

    @Override
    public void extract(HttpRequestEntity entity, String requestId) throws IOException {
        activeJobs.add(requestId);
        final PreReadEntity preReadEntity = new PreReadEntity(entity);
        ExtractorJob extractorJob = new ExtractorJob(preReadEntity, requestId);
        pendingJobs.add(extractorJob);
    }

    @Override
    public boolean isActive(String requestId) {
        return activeJobs.contains(requestId);
    }

    @Override
    public Set<MimeType> getSupportedInputFormats() {
        try {
            MimeType mimeType = new MimeType("text/plain");
            return Collections.singleton(mimeType);
        } catch (MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Set<MimeType> getSupportedOutputFormats() {
        return Collections.singleton(getOutputMimeType());
    }
    
    private MimeType getOutputMimeType() {
        try {
            MimeType mimeType = new MimeType("text/plain;stamped=true");
            return mimeType;
        } catch (MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    private class WorkerThread extends Thread {

        public WorkerThread() {
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(5*1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }                
                final ExtractorJob job = pendingJobs.poll();
                if (job != null)  {
                    final Entity responseEntity = new WritingEntity() {

                        @Override
                        public MimeType getType() {
                            return getOutputMimeType();
                        }

                        @Override
                        public void writeData(OutputStream out) throws IOException {
                            IOUtils.copy(job.entity.getData(), out);
                            out.flush();
                            out.write("\n ***** STAMPED *****\n".getBytes("UTF-8"));
                            out.flush();
                        }
                    };
                    callBackHandler.responseAvailable(job.requestId, responseEntity);
                    activeJobs.remove(job.requestId);
                }
            }
        }
        
        
    }

    private static class ExtractorJob {
        private final String requestId;
        private final HttpRequestEntity entity;

        public ExtractorJob(HttpRequestEntity entity, String requestId) {
            this.entity = entity;
            this.requestId = requestId;
        }
    }
    
}
