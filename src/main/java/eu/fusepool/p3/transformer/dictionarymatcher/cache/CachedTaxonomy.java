package eu.fusepool.p3.transformer.dictionarymatcher.cache;

import eu.fusepool.p3.transformer.dictionarymatcher.impl.DictionaryAnnotator;
import java.util.Calendar;

/**
 *
 * @author Gabor
 */
public class CachedTaxonomy {

    final private Calendar created;
    final private Object lock;

    private DictionaryAnnotator dictionaryAnnotator;

    public CachedTaxonomy() {
        dictionaryAnnotator = null;
        created = Calendar.getInstance();
        lock = new Object();
    }

    /**
     * Checks if taxonomy is loaded already.
     *
     * @return
     */
    public boolean isLoaded() {
        return dictionaryAnnotator != null;
    }

    /**
     * Checks if the cached instance is older than a day.
     *
     * @return
     */
    public Boolean isValid() {
        Calendar validDate = Calendar.getInstance();
        validDate.add(Calendar.HOUR_OF_DAY, -24);
        return validDate.before(created);
    }

    /**
     * Gets the lock instance.
     *
     * @return
     */
    public Object getLock() {
        return lock;
    }

    /**
     * Returns taxonomy instance.
     *
     * @return
     */
    public DictionaryAnnotator getTaxonomy() {
        return dictionaryAnnotator;
    }

    /**
     * Sets the taxonomy instance.
     *
     * @param dictionaryAnnotator
     */
    public void setTaxonomy(DictionaryAnnotator dictionaryAnnotator) {
        this.dictionaryAnnotator = dictionaryAnnotator;
    }
}
