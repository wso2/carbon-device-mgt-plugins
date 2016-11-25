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

package org.testing.Linux.plugin.impl;

import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testing.linux.plugin.exception.LinuxDeviceMgtPluginException;
import org.testing.linux.plugin.impl.dao.LinuxDAOUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import java.util.List;

public class LinuxManager implements DeviceManager{

	@Override
	public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException{
		try{
			if (log.isDebugEnabled()){
				log.debug(
					"updating the details : " + DeviceIdentifier);
			}
			LinuxDAOUtil.beginTransaction();
			status = LinuxDAOUtil.getLinuxDeviceDAO().updateDevice(device);
			LinuxDAOUtil.commitTransaction();
		} catch (LinuxDeviceMgtPluginException e){
			try{
				LinuxDAOUtil.rollbackTransaction();
			} catch (LinuxDeviceMgtPluginException iotDAOEx){
				String msg = "Error occured while roll back the update device info transaction :" + device.toString();
				log.warn(msg,iotDAOEx);
			}
				String msg = "Error while updating the device : " + deviceIdentifier;
				log.error(msg, e);
				throw new DeviceManagementException(msg,e);
		}
		return status;
	}
}