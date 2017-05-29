/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.mobile.windows.api.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsDeviceEnrolmentException;
import org.wso2.carbon.device.mgt.mobile.windows.exception.WindowsDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.mobile.windows.impl.WindowsTokenService;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dto.MobileCacheEntry;
import org.wso2.carbon.device.mgt.mobile.windows.impl.util.WindowsUtils;

import java.util.UUID;

/**
 * Class for generate random token for XCEP and WSTEP.
 */
public class DeviceUtil {

    private static final String TOKEN_CACHE_MANAGER = "TOKEN_CACHE_MANAGER";
    private static final String TOKEN_CACHE = "TOKEN_CACHE";
    private static final long CACHE_DURATION = 15l;
    private static boolean isContextCacheInitialized = false;
    private static WindowsTokenService tokenService;

    private static final Log log = LogFactory.getLog(DeviceUtil.class);

    static {
        try {
            tokenService = WindowsUtils.getEnrollmentTokenService();
        } catch (WindowsDeviceMgtPluginException e) {
            log.error("WindowsTokenService cannot be initialized.", e);
        }
    }

    public static String generateRandomToken() {
        return String.valueOf(UUID.randomUUID());
    }

    public static void persistChallengeToken(String token, String deviceID, String username)
            throws  WindowsDeviceEnrolmentException {
        try {
            if(tokenService == null) {
                tokenService = WindowsUtils.getEnrollmentTokenService();
            }
            MobileCacheEntry existingCacheEntry = tokenService.getCacheToken(token);
            PrivilegedCarbonContext carbonCtx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            if (existingCacheEntry == null) {
                MobileCacheEntry newCacheEntry = new MobileCacheEntry();
                newCacheEntry.setDeviceID(deviceID);
                newCacheEntry.setUsername(username);
                newCacheEntry.setCacheToken(token);
                newCacheEntry.setTenantDomain(carbonCtx.getTenantDomain());
                newCacheEntry.setTenanatID(carbonCtx.getTenantId());
                tokenService.saveCacheToken(newCacheEntry);
            } else {
                existingCacheEntry.setDeviceID(deviceID);
                existingCacheEntry.setCacheToken(token);
                tokenService.updateCacheToken(existingCacheEntry);
            }
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error occured when saving cache token for device: " + deviceID;
            log.error(msg);
            throw new WindowsDeviceEnrolmentException(msg, e);
        } catch (WindowsDeviceMgtPluginException e) {
            String msg = "Error occured when retrieving enrollment token service";
            log.error(msg);
            throw new WindowsDeviceEnrolmentException(msg, e);
        }
    }

    public static MobileCacheEntry getTokenEntry(String token)
            throws WindowsDeviceEnrolmentException {
        MobileCacheEntry tokenEntry;
        try {
            if (tokenService == null) {
                    tokenService = WindowsUtils.getEnrollmentTokenService();
            }
            tokenEntry = tokenService.getCacheToken(token);
        } catch (WindowsDeviceMgtPluginException e) {
            String msg = "Error occured when retrieving enrollment token service.";
            log.error(msg);
            throw new WindowsDeviceEnrolmentException(msg, e);
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error occured when retrieving enrollment token.";
            log.error(msg);
            throw new WindowsDeviceEnrolmentException(msg, e);
        }
        return tokenEntry;
    }

    public static MobileCacheEntry getTokenEntryFromDeviceId(String deviceId)
            throws WindowsDeviceEnrolmentException {
        MobileCacheEntry tokenEntry;
        try {
            if (tokenService == null) {
                tokenService = WindowsUtils.getEnrollmentTokenService();
            }
            tokenEntry = tokenService.getCacheTokenFromDeviceId(deviceId);
        } catch (WindowsDeviceMgtPluginException e) {
            String msg = "Error occured when retrieving enrollment token service.";
            log.error(msg);
            throw new WindowsDeviceEnrolmentException(msg, e);
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error occured when retrieving enrollment token.";
            log.error(msg);
            throw new WindowsDeviceEnrolmentException(msg, e);
        }
        return tokenEntry;
    }

    public static void removeTokenEntry(String token) {
        try {
            if (tokenService == null) {
                tokenService = WindowsUtils.getEnrollmentTokenService();
            }
            tokenService.removeCacheToken(token);
        } catch (WindowsDeviceMgtPluginException e) {
            String msg = "Error occured when retrieving enrollment token service.";
            log.error(msg);
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error occured when removing enrollment token.";
            log.error(msg);
        }
    }
}
