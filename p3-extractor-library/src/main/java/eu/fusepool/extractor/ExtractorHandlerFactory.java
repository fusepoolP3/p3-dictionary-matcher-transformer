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

/**
 *
 * @author reto
 */
public class ExtractorHandlerFactory {
    public static AbstractExtractingHandler getExtractorHandler(SyncExtractor extractor) {
       if (extractor.isLongRunning()) {
            return new AsyncExtractorHandler(new LongRunningExtractorWrapper(extractor));
        } else {
            return new SyncExtractorHandler(extractor);
        }
    }
    
    public static AbstractExtractingHandler getExtractorHandler(AsyncExtractor extractor) {
        return new AsyncExtractorHandler(extractor);
    }
    
    public static AbstractExtractingHandler getExtractorHandler(Extractor extractor) {
        if (extractor instanceof SyncExtractor) {
            return getExtractorHandler((SyncExtractor)extractor);
        } else {
            return getExtractorHandler((AsyncExtractor)extractor);
        }
        
    }
}
