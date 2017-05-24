/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.mobile.windows.impl;

import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dto.MobileCacheEntry;

public interface WindowsTokenService {
    void saveCacheToken(MobileCacheEntry entry) throws MobileDeviceManagementDAOException;
    void updateCacheToken(MobileCacheEntry entry) throws MobileDeviceManagementDAOException;
    MobileCacheEntry getCacheToken(String token) throws MobileDeviceManagementDAOException;
    MobileCacheEntry getCacheTokenFromDeviceId(String deviceId) throws MobileDeviceManagementDAOException;
    void removeCacheToken(String token) throws MobileDeviceManagementDAOException;
}
