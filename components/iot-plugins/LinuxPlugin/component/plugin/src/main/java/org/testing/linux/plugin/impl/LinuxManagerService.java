/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.testing.linux.plugin.impl;

import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import java.util.List;

public class LinuxManagerService implements DeviceManagementService{
	private DeviceManager deviceManager;

	@Override
	public String getType(){
		return LinuxConstants.DEVICE_TYPE;
	}

	@Override
	public void init() throws DeviceManagementException{
		this.deviceManager = new LinuxManager();
	}

	@Override
	public DeviceManager getDeviceManager(){
		return deviceManager;
	}

	@Override
	public ApplicationManager getApplicationManager(){
		return null;
	}

	@Override
	public getProvisioningConfig(){
		return new ProvisioningConfig("carbon.super",false);
	}

	@Override
	public PushNotificationConfig getPushNotificationConfig(){
		return null;
	}
}