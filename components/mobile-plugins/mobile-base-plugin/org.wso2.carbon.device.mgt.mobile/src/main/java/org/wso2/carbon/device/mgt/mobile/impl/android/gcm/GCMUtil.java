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

import com.google.gson.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.mobile.impl.android.AndroidDeviceManagementService;
import org.wso2.carbon.device.mgt.mobile.impl.android.util.AndroidPluginConstants;
import org.wso2.carbon.device.mgt.mobile.internal.MobileDeviceManagementDataHolder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Implements utility methods used by GCMService.
 */
public class GCMUtil {

    private static final Log log = LogFactory.getLog(GCMService.class);

    private final static String GCM_ENDPOINT = "https://gcm-http.googleapis.com/gcm/send";
    private static final String GCM_API_KEY = "gcmAPIKey";
    private static final int TIME_TO_LIVE = 60;
    private static final int HTTP_STATUS_CODE_OK = 200;

    private static HashMap<Integer, PlatformConfiguration> tenantConfigurationCache = new HashMap<>();

    public static GCMResult sendWakeUpCall(String message, List<Device> devices) {
        GCMResult result = new GCMResult();

        byte[] bytes = getGCMRequest(message, getGCMTokens(devices)).getBytes();
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) (new URL(GCM_ENDPOINT)).openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=" + getConfigurationProperty(GCM_API_KEY));

            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();

            int status = conn.getResponseCode();
            result.setStatusCode(status);
            if (status != HTTP_STATUS_CODE_OK) {
                result.setErrorMsg(getString(conn.getErrorStream()));
            } else {
                result.setMsg(getString(conn.getInputStream()));
            }
        } catch (ProtocolException e) {
            log.error("Exception occurred while setting the HTTP protocol.", e);
        } catch (IOException ex) {
            log.error("Exception occurred while sending the GCM request.", ex);
        }

        return result;
    }

    private static String getString(InputStream stream) throws IOException {
        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder content = new StringBuilder();

            String newLine;
            do {
                newLine = reader.readLine();
                if (newLine != null) {
                    content.append(newLine).append('\n');
                }
            } while (newLine != null);

            if (content.length() > 0) {
                content.setLength(content.length() - 1);
            }

            return content.toString();
        }
        return null;
    }

    private static String getGCMRequest(String message, List<String> registrationIds) {
        JsonObject gcmRequest = new JsonObject();
        gcmRequest.addProperty("delay_while_idle", false);
        gcmRequest.addProperty("time_to_live", TIME_TO_LIVE);

        //Add message to GCM request
        JsonObject data = new JsonObject();
        if (message != null && !message.isEmpty()) {
            data.addProperty("data", message);
            gcmRequest.add("data", data);
        }

        //Set device reg-ids
        JsonArray regIds = new JsonArray();
        for (String regId : registrationIds) {
            if (regId == null || regId.isEmpty()) {
                continue;
            }
            regIds.add(new JsonPrimitive(regId));
        }

        gcmRequest.add("registration_ids", regIds);
        return gcmRequest.toString();
    }

    private static List<String> getGCMTokens(List<Device> devices) {
        List<String> tokens = new ArrayList<>(devices.size());
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

    public static String getConfigurationProperty(String property) {
        DeviceManagementService androidDMService = new AndroidDeviceManagementService();
        try {
            //Get the TenantConfiguration from cache if not we'll get it from DM service
            PlatformConfiguration tenantConfiguration = getTenantConfigurationFromCache();
            if (tenantConfiguration == null) {
                androidDMService.init();
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

    public static void resetTenantConfigCache() {
        tenantConfigurationCache.remove(getTenantId());
    }

    private static void addTenantConfigurationToCache(PlatformConfiguration tenantConfiguration) {
        tenantConfigurationCache.put(getTenantId(), tenantConfiguration);
    }

    private static PlatformConfiguration getTenantConfigurationFromCache() {
        return tenantConfigurationCache.get(getTenantId());
    }

    private static int getTenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }
}