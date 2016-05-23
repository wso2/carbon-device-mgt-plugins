package org.wso2.carbon.mdm.services.android.omadm.cachemanager.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mdm.services.android.omadm.cachemanager.OMADMCacheManager;
import org.wso2.carbon.mdm.services.android.omadm.cachemanager.OMADMCacheUtils;
import org.wso2.carbon.mdm.services.android.omadm.cachemanager.beans.DMTreeOperationCacheEntry;

import javax.cache.Cache;

/**
 * This class manages all the cache related operations used in OMADM management sessions
 */
public class OMADMCacheManagerImpl implements OMADMCacheManager {

    private static final Log log = LogFactory.getLog(OMADMCacheManagerImpl.class);
    private static OMADMCacheManagerImpl cacheManager;
    private static final String OMADM_CACHE_MANAGER = "OMADM_CACHE_MANAGER";
    private static final String OMADM_CACHE = "OMADM_CACHE";
    private static final long CACHE_EXPIRY = 15l;
    private static boolean isInitialized = false;

    private OMADMCacheManagerImpl() {}

    public static OMADMCacheManagerImpl getInstance() {
        if (cacheManager == null) {
            synchronized (OMADMCacheManagerImpl.class) {
                if (cacheManager == null) {
                    cacheManager = new OMADMCacheManagerImpl();
                }
            }
        }
        return cacheManager;
    }

    private static Cache<String, DMTreeOperationCacheEntry> getOperationListCache() {
        return OMADMCacheUtils.getOMADMCache();
    }

    @Override
    public void addOperationEntry(String deviceId, DMTreeOperationCacheEntry entry) {
        Object cacheEntry = getOperationEntry(deviceId);
        if (cacheEntry != null) {
            getOperationListCache().remove(deviceId);
        } else {
            getOperationListCache().put(deviceId, entry);
        }
    }

    @Override
    public DMTreeOperationCacheEntry getOperationEntry(String deviceId) {
        return getOperationListCache().get(deviceId);
    }

}
