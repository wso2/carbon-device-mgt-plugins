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

package org.wso2.carbon.device.mgt.mobile.impl.windows;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;
import org.wso2.carbon.device.mgt.mobile.impl.windows.dao.WindowsDAOFactory;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;

import java.util.List;

/**
 * This represents the Windows implementation of DeviceManagerService.
 */
public class WindowsDeviceManagementService implements DeviceManagementService {

     private MobileDeviceManagementDAOFactory mobileDeviceManagementDAOFactory;

    public WindowsDeviceManagementService() {
        mobileDeviceManagementDAOFactory = new WindowsDAOFactory();
    }
    private static final Log log = LogFactory.getLog(WindowsDeviceManagementService.class);

    @Override
    public String getProviderType() {
        return DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return null;
    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceId, boolean status)
            throws DeviceManagementException {
        return true;
    }

    public List<Device> getAllDevices() throws DeviceManagementException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType)
            throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean isClaimable(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setStatus(DeviceIdentifier deviceIdentifier, String currentUser,
                             EnrolmentInfo.Status status) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException {
        return true;
    }

	@Override
	public boolean enrollDevice(Device device) throws DeviceManagementException {
		boolean status;
		MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
		try {
			status = mobileDeviceManagementDAOFactory.getMobileDeviceDAO().addMobileDevice(
					mobileDevice);
		} catch (MobileDeviceManagementDAOException e) {
			String msg = "Error while enrolling the Windows device : " +
			             device.getDeviceIdentifier();
			log.error(msg, e);
			throw new DeviceManagementException(msg, e);
		}
		return status;
	}

    @Override
    public Application[] getApplications(String s, int i, int i2) throws ApplicationManagementException {
        return new Application[0];
    }

    @Override
    public void updateApplicationStatus(DeviceIdentifier deviceIdentifier,
                                        Application application, String s) throws ApplicationManagementException {

    }

    @Override
    public String getApplicationStatus(DeviceIdentifier deviceIdentifier,
                                       Application application) throws ApplicationManagementException {
        return null;
    }

    @Override
    public void installApplication(Operation operation,
                                   List<DeviceIdentifier> deviceIdentifiers) throws ApplicationManagementException {

    }

}
