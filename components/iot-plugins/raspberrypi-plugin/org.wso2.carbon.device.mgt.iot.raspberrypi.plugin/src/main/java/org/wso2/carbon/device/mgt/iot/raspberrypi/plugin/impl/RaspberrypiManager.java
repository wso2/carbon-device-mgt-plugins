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

package org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.sensor.mgt.SensorManager;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.exception.RaspberrypiDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.impl.dao.RaspberrypiDAOUtil;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.impl.feature.RaspberrypiFeatureManager;

import java.util.List;

/**
 * This represents the Raspberrypi implementation of DeviceManagerService.
 */
public class RaspberrypiManager implements DeviceManager {

    private static final RaspberrypiDAOUtil raspberrypiDAO = new RaspberrypiDAOUtil();
    private static final Log log = LogFactory.getLog(RaspberrypiManager.class);
    private RaspberrypiFeatureManager raspberrypiFeatureManager = new RaspberrypiFeatureManager();
    @Override
    public FeatureManager getFeatureManager() {
        return raspberrypiFeatureManager;
    }

    @Override
    public SensorManager getSensorManager() {
        return null;
    }

    @Override
    public boolean saveConfiguration(PlatformConfiguration tenantConfiguration)
            throws DeviceManagementException {
        return false;
    }

    @Override
    public PlatformConfiguration getConfiguration() throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Enrolling a new Raspberrypi device : " + device.getDeviceIdentifier());
            }
            RaspberrypiDAOUtil.beginTransaction();
            status = raspberrypiDAO.getDeviceDAO().addDevice(device);
            RaspberrypiDAOUtil.commitTransaction();
        } catch (RaspberrypiDeviceMgtPluginException e) {
            try {
                RaspberrypiDAOUtil.rollbackTransaction();
            } catch (RaspberrypiDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the device enrol transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while enrolling the Raspberrypi device : " + device.getDeviceIdentifier();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Modifying the Raspberrypi device enrollment data");
            }
            RaspberrypiDAOUtil.beginTransaction();
            status = raspberrypiDAO.getDeviceDAO().updateDevice(device);
            RaspberrypiDAOUtil.commitTransaction();
        } catch (RaspberrypiDeviceMgtPluginException e) {
            try {
                RaspberrypiDAOUtil.rollbackTransaction();
            } catch (RaspberrypiDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the update device transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while updating the enrollment of the Raspberrypi device : " +
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
                log.debug("Dis-enrolling Raspberrypi device : " + deviceId);
            }
            RaspberrypiDAOUtil.beginTransaction();
            status = raspberrypiDAO.getDeviceDAO().deleteDevice(deviceId.getId());
            RaspberrypiDAOUtil.commitTransaction();
        } catch (RaspberrypiDeviceMgtPluginException e) {
            try {
                RaspberrypiDAOUtil.rollbackTransaction();
            } catch (RaspberrypiDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the device dis enrol transaction :" + deviceId.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while removing the Raspberrypi device : " + deviceId.getId();
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
                log.debug("Checking the enrollment of Raspberrypi device : " + deviceId.getId());
            }
            Device iotDevice = raspberrypiDAO.getDeviceDAO().getDevice(deviceId.getId());
            if (iotDevice != null) {
                isEnrolled = true;
            }
        } catch (RaspberrypiDeviceMgtPluginException e) {
            String msg = "Error while checking the enrollment status of Raspberrypi device : " +
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
                log.debug("Getting the details of Raspberrypi device : " + deviceId.getId());
            }
            device = raspberrypiDAO.getDeviceDAO().getDevice(deviceId.getId());
        } catch (RaspberrypiDeviceMgtPluginException e) {
            String msg = "Error while fetching the Raspberrypi device : " + deviceId.getId();
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

    public boolean isClaimable(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setStatus(DeviceIdentifier deviceId, String currentOwner,
                             EnrolmentInfo.Status status) throws DeviceManagementException {
        return false;
    }

    @Override
    public License getLicense(String s) throws LicenseManagementException {
        return null;
    }

    @Override
    public void addLicense(License license) throws LicenseManagementException {

    }

    @Override
    public boolean requireDeviceAuthorization() {
        return false;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException {
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "updating the details of Raspberrypi device : " + deviceIdentifier);
            }
            RaspberrypiDAOUtil.beginTransaction();
            status = raspberrypiDAO.getDeviceDAO().updateDevice(device);
            RaspberrypiDAOUtil.commitTransaction();
        } catch (RaspberrypiDeviceMgtPluginException e) {
            try {
                RaspberrypiDAOUtil.rollbackTransaction();
            } catch (RaspberrypiDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the update device info transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg =
                    "Error while updating the Raspberrypi device : " + deviceIdentifier;
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
                log.debug("Fetching the details of all Raspberrypi devices");
            }
            List<Device> iotDevices = raspberrypiDAO.getDeviceDAO().getAllDevices();
        } catch (RaspberrypiDeviceMgtPluginException e) {
            String msg = "Error while fetching all Raspberrypi devices.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return devices;
    }
}