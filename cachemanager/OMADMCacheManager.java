package org.wso2.carbon.mdm.services.android.omadm.cachemanager;

import org.wso2.carbon.mdm.services.android.omadm.cachemanager.beans.DMTreeOperationCacheEntry;

import javax.cache.Cache;

/**
 * The interface for the OMADM Cache Manager. The OMADMCacheManager
 * handles all the cahce-related operations of an OMADM management session
 */
public interface OMADMCacheManager {

    /**
     * Adds an DMTreeOperationCacheEntry to the cache
     *
     * @param deviceId - Device Identifier
     * @param entry - DMTreeOperationCacheEntry
     */
    void addOperationEntry(String deviceId, DMTreeOperationCacheEntry entry);

    /**
     * Returns the cache entry of a given Device Identifier
     *
     * @param deviceId - Device Identifier
     * @return - DMTreeOperationCacheEntry
     */
    DMTreeOperationCacheEntry getOperationEntry(String deviceId);

}
