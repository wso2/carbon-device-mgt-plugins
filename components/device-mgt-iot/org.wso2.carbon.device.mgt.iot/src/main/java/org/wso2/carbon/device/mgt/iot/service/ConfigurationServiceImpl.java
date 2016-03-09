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

package org.wso2.carbon.device.mgt.iot.service;

import org.wso2.carbon.device.mgt.iot.config.server.DeviceManagementConfigurationManager;
import org.wso2.carbon.device.mgt.iot.config.server.datasource.ControlQueue;
import org.wso2.carbon.device.mgt.iot.config.server.datasource.DeviceManagementConfiguration;
import org.wso2.carbon.device.mgt.iot.config.server.datasource.DeviceMgtServerInfo;

public class ConfigurationServiceImpl implements ConfigurationService {

    @Override
    public DeviceManagementConfiguration getDeviceCloudMgtConfig() {
        return DeviceManagementConfigurationManager.getInstance().getDeviceCloudMgtConfig();
    }

    @Override
    public ControlQueue getControlQueue(String name) {
        return DeviceManagementConfigurationManager.getInstance().getControlQueue(name);
    }

    @Override
    public DeviceMgtServerInfo getDeviceManagementServerInfo() {
        return DeviceManagementConfigurationManager.getInstance().getDeviceManagementServerInfo();
    }

}
