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

package org.wso2.carbon.device.mgt.mobile.impl.ios;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.spi.DeviceManager;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;

import java.util.List;

/**
 * This represents the iOS implementation of DeviceManagerService.
 */
public class IOSDeviceManager implements DeviceManager {

    private static final Log log = LogFactory.getLog(IOSDeviceManager.class);
    private MobileDeviceManagementDAOFactory mobileDeviceManagementDAOFactory;
    private IOSFeatureManager iosFeatureManager;

    @Override
    public String getProviderType() {
        return DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS;
    }

    public IOSDeviceManager() {
        mobileDeviceManagementDAOFactory = new MobileDeviceManagementDAOFactory(DeviceManagementConstants
                .MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS);
        iosFeatureManager = new IOSFeatureManager();
    }

    @Override
    public FeatureManager getFeatureManager() {
        return iosFeatureManager;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        boolean status;
        MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
        try {
            status = mobileDeviceManagementDAOFactory.getMobileDeviceDAO().addMobileDevice(
                    mobileDevice);
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error while enrolling the iOS device : " +
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
                log.debug("Modifying the iOS device enrollment data");
            }
            status = mobileDeviceManagementDAOFactory.getMobileDeviceDAO()
                    .updateMobileDevice(mobileDevice);
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error while updating the enrollment of the iOS device : " +
                    device.getDeviceIdentifier();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        boolean isEnrolled = false;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Checking the enrollment of iOS device : " + deviceId.getId());
            }
            MobileDevice mobileDevice =
                    mobileDeviceManagementDAOFactory.getMobileDeviceDAO().getMobileDevice(
                            deviceId.getId());
            if (mobileDevice != null) {
                isEnrolled = true;
            }
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error while checking the enrollment status of iOS device : " +
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
    public List<Device> getAllDevices() throws DeviceManagementException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        Device device;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting the details of iOS device : " + deviceId.getId());
            }
            MobileDevice mobileDevice = mobileDeviceManagementDAOFactory.getMobileDeviceDAO().
                    getMobileDevice(deviceId.getId());
            device = MobileDeviceManagementUtil.convertToDevice(mobileDevice);
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error while fetching the iOS device : " + deviceId.getId();
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
        return true;
    }

}
