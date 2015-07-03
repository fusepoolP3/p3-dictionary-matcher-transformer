package eu.fusepool.p3.transformer.dictionarymatcher.cache;

import eu.fusepool.p3.transformer.dictionarymatcher.impl.DictionaryAnnotator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Gabor
 */
public class Cache {

    private static final Map<String, CachedTaxonomy> taxonomyCache = new HashMap<>();

    /**
     * Registers a taxonomy instance.
     *
     * @param uri
     * @return
     */
    public static Object register(final String uri) {
        CachedTaxonomy temp = taxonomyCache.get(uri);
        if (temp == null) {
            temp = new CachedTaxonomy();
            taxonomyCache.put(uri, temp);
        }
        return temp.getLock();
    }

    /**
     * Checks if taxonomy is already in the cache.
     *
     * @param uri
     * @return
     */
    public static boolean containsTaxonomy(final String uri) {
        if (taxonomyCache.containsKey(uri)) {
            return taxonomyCache.get(uri).isLoaded();
        }
        return false;
    }

    /**
     * Get a taxnonomy from the cache.
     *
     * @param uri
     * @return
     */
    public static DictionaryAnnotator getTaxonomy(final String uri) {
        CachedTaxonomy temp = taxonomyCache.get(uri);
        if (temp != null) {
            return temp.getTaxonomy();
        }
        return null;
    }

    /**
     * Add a new taxonomy to the cache if it is not already in the cache.
     *
     * @param uri
     * @param dictionaryAnnotator
     */
    public static void setTaxonomy(final String uri, DictionaryAnnotator dictionaryAnnotator) {
        CachedTaxonomy temp = taxonomyCache.get(uri);
        if (temp != null) {
            temp.setTaxonomy(dictionaryAnnotator);
            taxonomyCache.put(uri, temp);
        }
    }
}
