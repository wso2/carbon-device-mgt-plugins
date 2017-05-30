/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.mobile.android.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.mobile.android.impl.util.AndroidPluginConstants;
import org.wso2.carbon.device.mgt.mobile.android.internal.AndroidDeviceManagementDataHolder;

import java.util.HashMap;
import java.util.List;

/**
 * This represents the Android implementation of DeviceManagerService.
 */
public class AndroidDeviceManagementService implements DeviceManagementService {

    private static final Log log = LogFactory.getLog(AndroidDeviceManagementService.class);
    private DeviceManager deviceManager;
    public static final String DEVICE_TYPE_ANDROID = "android";
    private static final String SUPER_TENANT_DOMAIN = "carbon.super";
    private static final String NOTIFIER_PROPERTY = "notifierType";
    private static final String FCM_API_KEY = "fcmAPIKey";
    private static final String FCM_SENDER_ID = "fcmSenderId";
    private PolicyMonitoringManager policyMonitoringManager;

    @Override
    public String getType() {
        return AndroidDeviceManagementService.DEVICE_TYPE_ANDROID;
    }

    @Override
    public OperationMonitoringTaskConfig getOperationMonitoringConfig() {
        return null;
    }

    @Override
    public void init() throws DeviceManagementException {
        this.deviceManager = new AndroidDeviceManager();
        this.policyMonitoringManager = new AndroidPolicyMonitoringManager();
    }

    @Override
    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    @Override
    public ApplicationManager getApplicationManager() {
        return null;
    }

    @Override
    public ProvisioningConfig getProvisioningConfig() {
        return new ProvisioningConfig(SUPER_TENANT_DOMAIN, true);
    }

    @Override
    public PushNotificationConfig getPushNotificationConfig() {
        try {
            DeviceManagementService deviceManagementService = AndroidDeviceManagementDataHolder.getInstance().
                    getAndroidDeviceManagementService();
            if (deviceManagementService != null && deviceManagementService.getDeviceManager() != null) {
                PlatformConfiguration androidConfig = deviceManagementService.getDeviceManager().getConfiguration();
                if (androidConfig != null) {
                    List<ConfigurationEntry> configuration = androidConfig.getConfiguration();
                    String notifierValue = this.getConfigProperty(configuration, NOTIFIER_PROPERTY);
                    if (notifierValue != null && !notifierValue.isEmpty()) {
                        int notifierType = Integer.parseInt(notifierValue);
                        if (notifierType == 2) {
                            HashMap<String, String> config = new HashMap<>();
                            config.put(FCM_API_KEY, this.getConfigProperty(configuration, FCM_API_KEY));
                            config.put(FCM_SENDER_ID, this.getConfigProperty(configuration, FCM_SENDER_ID));
                            return new PushNotificationConfig(AndroidPluginConstants.NotifierType.FCM, false,
                                    config);
                        }
                    }
                }
            }
        } catch (DeviceManagementException e) {
            log.error("Unable to get the Android platform configuration from registry.");
        }
        return null;
    }

    @Override
    public PolicyMonitoringManager getPolicyMonitoringManager() {
        return policyMonitoringManager;
    }

    @Override
    public InitialOperationConfig getInitialOperationConfig() {
        return null;
    }

    public DeviceStatusTaskPluginConfig getDeviceStatusTaskPluginConfig() {
        return null;
    }

    private String getConfigProperty(List<ConfigurationEntry> configs, String propertyName) {
        for (ConfigurationEntry entry : configs) {
            if (propertyName.equals(entry.getName())) {
                return entry.getValue().toString();
            }
        }
        return null;
    }
}
