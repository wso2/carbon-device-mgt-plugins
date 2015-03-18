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

package org.wso2.carbon.device.mgt.mobile.impl.android;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.spi.DeviceManager;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;
import org.wso2.carbon.device.mgt.common.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents the Android implementation of DeviceManagerService.
 */
public class AndroidDeviceManager implements DeviceManager {

	private static final Log log = LogFactory.getLog(AndroidDeviceManager.class);

	@Override
	public String getProviderType() {
		return DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID;
	}

    @Override
    public FeatureManager getFeatureManager() {
        return new AndroidFeatureManager();
    }

    @Override
	public boolean enrollDevice(Device device) throws DeviceManagementException {
		boolean status;
		MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
		try {
			if (log.isDebugEnabled()) {
				log.debug("Enrolling a new Android device : " + device.getDeviceIdentifier());
			}
			status = MobileDeviceManagementDAOFactory.getMobileDeviceDAO().addMobileDevice(
					mobileDevice);
		} catch (MobileDeviceManagementDAOException e) {
			String msg = "Error while enrolling the Android device : " +
			             device.getDeviceIdentifier();
			log.error(msg, e);
			throw new DeviceManagementException(msg, e);
		}
		return status;
	}

	@Override
	public boolean modifyEnrollment(Device device) throws DeviceManagementException {
		boolean status;
		MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
		try {
			if (log.isDebugEnabled()) {
				log.debug("Modifying the Android device enrollment data");
			}
			status = MobileDeviceManagementDAOFactory.getMobileDeviceDAO()
			                                         .updateMobileDevice(mobileDevice);
		} catch (MobileDeviceManagementDAOException e) {
			String msg = "Error while updating the enrollment of the Android device : " +
			             device.getDeviceIdentifier();
			log.error(msg, e);
			throw new DeviceManagementException(msg, e);
		}
		return status;
	}

	@Override
	public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
		boolean status;
		try {
			if (log.isDebugEnabled()) {
				log.debug("Dis-enrolling Android device : " + deviceId);
			}
			status = MobileDeviceManagementDAOFactory.getMobileDeviceDAO()
			                                         .deleteMobileDevice(deviceId.getId());
		} catch (MobileDeviceManagementDAOException e) {
			String msg = "Error while removing the Android device : " + deviceId.getId();
			log.error(msg, e);
			throw new DeviceManagementException(msg, e);
		}
		return status;
	}

	@Override
	public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
		boolean isEnrolled = false;
		try {
			if (log.isDebugEnabled()) {
				log.debug("Checking the enrollment of Android device : " + deviceId.getId());
			}
			MobileDevice mobileDevice =
					MobileDeviceManagementDAOFactory.getMobileDeviceDAO().getMobileDevice(
							deviceId.getId());
			if (mobileDevice != null) {
				isEnrolled = true;
			}
		} catch (MobileDeviceManagementDAOException e) {
			String msg = "Error while checking the enrollment status of Android device : " +
			             deviceId.getId();
			log.error(msg, e);
			throw new DeviceManagementException(msg, e);
		}
		return isEnrolled;
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

	@Override
	public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
		Device device;
		try {
			if (log.isDebugEnabled()) {
				log.debug("Getting the details of Android device : " + deviceId.getId());
			}
			MobileDevice mobileDevice = MobileDeviceManagementDAOFactory.getMobileDeviceDAO().
					getMobileDevice(deviceId.getId());
			device = MobileDeviceManagementUtil.convertToDevice(mobileDevice);
		} catch (MobileDeviceManagementDAOException e) {
			String msg = "Error while fetching the Android device : " + deviceId.getId();
			log.error(msg, e);
			throw new DeviceManagementException(msg, e);
		}
		return device;
	}

	@Override
	public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType)
			throws DeviceManagementException {
		return true;
	}

	@Override
	public boolean updateDeviceInfo(Device device) throws DeviceManagementException {
		boolean status;
		MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
		try {
			if (log.isDebugEnabled()) {
				log.debug("updating the details of Android device : " + device.getDeviceIdentifier());
			}
			status = MobileDeviceManagementDAOFactory.getMobileDeviceDAO()
			                                         .updateMobileDevice(mobileDevice);
		} catch (MobileDeviceManagementDAOException e) {
			String msg = "Error while updating the Android device : " + device.getDeviceIdentifier();
			log.error(msg, e);
			throw new DeviceManagementException(msg, e);
		}
		return status;
	}

	@Override
	public List<Device> getAllDevices() throws DeviceManagementException {
		List<Device> devices = null;
		try {
			if (log.isDebugEnabled()) {
				log.debug("Fetching the details of all Android devices");
			}
			List<MobileDevice> mobileDevices =
					MobileDeviceManagementDAOFactory.getMobileDeviceDAO().
							getAllMobileDevices();
			if (mobileDevices != null) {
				devices = new ArrayList<Device>();
                for (MobileDevice mobileDevice : mobileDevices) {
                    devices.add(MobileDeviceManagementUtil.convertToDevice(mobileDevice));
                }
			}
		} catch (MobileDeviceManagementDAOException e) {
			String msg = "Error while fetching all Android devices.";
			log.error(msg, e);
			throw new DeviceManagementException(msg, e);
		}
		return devices;
	}

}