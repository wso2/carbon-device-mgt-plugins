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

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.mobile.impl.android.util.AndroidPluginConstants;
import org.wso2.carbon.device.mgt.mobile.internal.MobileDeviceManagementDataHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * GCM notification service implementation for Android platform.
 */
public class GCMService {

    private static final Log log = LogFactory.getLog(GCMService.class);
    public static final String GCM_APIKEY = "gcmAPIKey";
    public static final String NOTIFIER_TYPE = "notifierType";
    public static final String GCM_NOTIFIER_CODE = "2";
    private static HashMap<Integer,TenantConfiguration> tenantConfigurationCache = new HashMap<>();

    public boolean isGCMEnabled() {
        String notifierType = getConfigurationProperty(NOTIFIER_TYPE);
        if (GCM_NOTIFIER_CODE.equals(notifierType)) {
            return true;
        }
        return false;
    }

    public void sendNotification(String messageData, Device device) {
        int seconds = 60;
        Sender sender = new Sender(getConfigurationProperty(GCM_APIKEY));
        Message message =
                new Message.Builder().timeToLive(seconds).delayWhileIdle(false).addData("data", messageData).build();
        try {
            sender.send(message, getGCMToken(device.getProperties()), 5);
        } catch (IOException e) {
            log.error("Exception occurred while sending the GCM notification.",e);
        }
    }

    public void sendNotification(String messageData, List<Device> devices) {
        int seconds = 60;
        Sender sender = new Sender(getConfigurationProperty(GCM_APIKEY));
        Message message =
                new Message.Builder().timeToLive(seconds).delayWhileIdle(false).addData("data", messageData).build();
        try {
            sender.send(message, getGCMTokens(devices), 5);
        } catch (IOException e) {
            log.error("Exception occurred while sending the GCM notification.",e);
        }
    }

    private static List<String> getGCMTokens(List<Device> devices) {
        List<String> tokens = new ArrayList<>();
        for (Device device : devices) {
            tokens.add(getGCMToken(device.getProperties()));
        }
        return tokens;
    }

    private static String getGCMToken(List<Device.Property> properties) {
        String gcmToken = null;
        for (Device.Property property : properties) {
            if (AndroidPluginConstants.GCM_TOKEN.equals(property.getName())) {
                gcmToken = property.getValue();
                break;
            }
        }
        return gcmToken;
    }

    private static String getConfigurationProperty(String property) {
        DeviceManagementService androidDMService = MobileDeviceManagementDataHolder.getInstance().
                getAndroidDeviceManagementService();
        try {
            //Get the TenantConfiguration from cache if not we'll get it from DM service
            TenantConfiguration tenantConfiguration = getTenantConfigurationFromCache();
            if (tenantConfiguration == null) {
                tenantConfiguration = androidDMService.getDeviceManager().getConfiguration();
                if (tenantConfiguration != null) {
                    addTenantConfigurationToCache(tenantConfiguration);
                }
            }

            if (tenantConfiguration != null) {
                List<ConfigurationEntry> configs = tenantConfiguration.getConfiguration();
                for (ConfigurationEntry entry : configs) {
                    if (property.equals(entry.getName())) {
                        return (String) entry.getValue();
                    }
                }
            }
            return "";
        } catch (DeviceManagementException e) {
            log.error("Exception occurred while fetching the tenant-config.",e);
        }
        return null;
    }

    private static void addTenantConfigurationToCache(TenantConfiguration tenantConfiguration) {
        tenantConfigurationCache.put(getTenantId(), tenantConfiguration);
    }

    private static TenantConfiguration getTenantConfigurationFromCache() {
        return tenantConfigurationCache.get(getTenantId());
    }

    private static int getTenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }
}