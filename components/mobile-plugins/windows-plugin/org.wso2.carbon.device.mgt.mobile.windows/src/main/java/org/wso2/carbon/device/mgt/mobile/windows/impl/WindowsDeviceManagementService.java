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

package org.wso2.carbon.device.mgt.mobile.windows.impl;

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.ProvisioningConfig;
import org.wso2.carbon.device.mgt.common.TaskOperation;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;

import java.util.List;

/**
 * This represents the Windows implementation of DeviceManagerService.
 */
public class WindowsDeviceManagementService implements DeviceManagementService {

    private DeviceManager deviceManager;
    public static final String DEVICE_TYPE_WINDOWS = "windows";
    private final static String DEVICE_TYPE_PROVIDER_DOMAIN = "carbon.super";

    @Override
    public String getType() {
        return WindowsDeviceManagementService.DEVICE_TYPE_WINDOWS;
    }

    @Override
    public List<TaskOperation> getTasksForPlatform() {
        return null;
    }

    @Override
    public void init() throws DeviceManagementException {
        this.deviceManager = new WindowsDeviceManager();
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
        return new ProvisioningConfig(DEVICE_TYPE_PROVIDER_DOMAIN, true);
    }

    @Override
    public PushNotificationConfig getPushNotificationConfig() {
        return null;
    }

}
