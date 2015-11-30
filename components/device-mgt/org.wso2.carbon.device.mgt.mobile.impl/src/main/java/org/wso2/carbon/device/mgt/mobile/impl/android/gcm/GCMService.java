/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.mobile.impl.android.gcm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * GCM notification service implementation for Android platform.
 */
public class GCMService {

    private static final Log log = LogFactory.getLog(GCMService.class);
    private static final String NOTIFIER_TYPE = "notifierType";
    private static final String GCM_NOTIFIER_CODE = "2";

    public boolean isGCMEnabled() {
        String notifierType = GCMUtil.getConfigurationProperty(NOTIFIER_TYPE);
        if (GCM_NOTIFIER_CODE.equals(notifierType)) {
            return true;
        }
        return false;
    }

    public void sendNotification(String messageData, Device device) {
        List<Device> devices = new ArrayList<>();
        devices.add(device);
        GCMResult result = GCMUtil.sendWakeUpCall(messageData, devices);
        if (result.getStatusCode() != 200) {
            log.error("Exception occurred while sending the GCM notification : " + result.getErrorMsg());
        }
    }

    public void sendNotification(String messageData, List<Device> devices) {
        GCMResult result = GCMUtil.sendWakeUpCall(messageData, devices);
        if (result.getStatusCode() != 200) {
            log.error("Exception occurred while sending the GCM notification : " + result.getErrorMsg());
        }
    }

    public void resetTenantConfigCache() {
        GCMUtil.resetTenantConfigCache();
    }
}