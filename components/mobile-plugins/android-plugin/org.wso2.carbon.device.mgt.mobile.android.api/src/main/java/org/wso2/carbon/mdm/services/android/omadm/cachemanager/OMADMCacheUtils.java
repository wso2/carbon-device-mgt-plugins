package org.wso2.carbon.mdm.services.android.omadm.cachemanager;


import org.wso2.carbon.mdm.services.android.omadm.cachemanager.beans.DMTreeOperationCacheEntry;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.List;

/**
 * This is a utility class which contains various utilities related to the cache
 */
public class OMADMCacheUtils {

    private static final String OMADM_CACHE_MANAGER = "OMADM_CACHE_MANAGER";
    private static final String OMADM_CACHE = "OMADM_CACHE";

    /**
     * Returns the Chache Manager for OMADM operations
     *
     * @return - Cache Manager
     */
    private static CacheManager getCacheManager() {
        return Caching.getCacheManagerFactory().getCacheManager(OMADM_CACHE_MANAGER);
    }

    /**
     * Returns the OMADM Cache
     *
     * @return - OMADM Cache
     */
    public static Cache getOMADMCache() {
        CacheManager manager = getCacheManager();
        return (manager != null) ? manager.<String, List<DMTreeOperationCacheEntry>>getCache(OMADM_CACHE) :
                Caching.getCacheManager().<String, List<DMTreeOperationCacheEntry>>getCache(OMADM_CACHE);
    }

}
