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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.exception.VirtualFirealarmDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.dao.VirtualFireAlarmDAOUtil;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.feature.VirtualFirealarmFeatureManager;
import java.util.List;


/**
 * This represents the FireAlarm implementation of DeviceManagerService.
 */
public class VirtualFireAlarmManager implements DeviceManager {

    private static final VirtualFireAlarmDAOUtil virtualFireAlarmDAO = new VirtualFireAlarmDAOUtil();
    private static final Log log = LogFactory.getLog(VirtualFireAlarmManager.class);
    private FeatureManager virtualFirealarmFeatureManager = new VirtualFirealarmFeatureManager();

    @Override
    public FeatureManager getFeatureManager() {
        return virtualFirealarmFeatureManager;
    }

    @Override
    public boolean saveConfiguration(TenantConfiguration tenantConfiguration)
            throws DeviceManagementException {
        return false;
    }

    @Override
    public TenantConfiguration getConfiguration() throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Enrolling a new Virtual Firealarm device : " + device.getDeviceIdentifier());
            }
            VirtualFireAlarmDAOUtil.beginTransaction();
            status = virtualFireAlarmDAO.getDeviceDAO().addDevice(device);
            VirtualFireAlarmDAOUtil.commitTransaction();
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the device enrol transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while enrolling the Virtual Firealarm device : " + device.getDeviceIdentifier();
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
                log.debug("Modifying the Virtual Firealarm device enrollment data");
            }
            VirtualFireAlarmDAOUtil.beginTransaction();
            status = virtualFireAlarmDAO.getDeviceDAO().updateDevice(device);
            VirtualFireAlarmDAOUtil.commitTransaction();
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the update device transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while updating the enrollment of the Virtual Firealarm device : " +
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
                log.debug("Dis-enrolling Virtual Firealarm device : " + deviceId);
            }
            VirtualFireAlarmDAOUtil.beginTransaction();
            status = virtualFireAlarmDAO.getDeviceDAO().deleteDevice(deviceId.getId());
            VirtualFireAlarmDAOUtil.commitTransaction();
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the device dis enrol transaction :" + deviceId.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while removing the Virtual Firealarm device : " + deviceId.getId();
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
                log.debug("Checking the enrollment of Virtual Firealarm device : " + deviceId.getId());
            }
            Device device = virtualFireAlarmDAO.getDeviceDAO().getDevice(deviceId.getId());
            if (device != null) {
                isEnrolled = true;
            }
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error while checking the enrollment status of Virtual Firealarm device : " +
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
                log.debug("Getting the details of Virtual Firealarm device : " + deviceId.getId());
            }
            device = virtualFireAlarmDAO.getDeviceDAO().getDevice(deviceId.getId());
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error while fetching the Virtual Firealarm device : " + deviceId.getId();
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
                        "updating the details of Virtual Firealarm device : " + deviceIdentifier);
            }
            VirtualFireAlarmDAOUtil.beginTransaction();
            status = virtualFireAlarmDAO.getDeviceDAO().updateDevice(device);
            VirtualFireAlarmDAOUtil.commitTransaction();
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the update device info transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg =
                    "Error while updating the Virtual Firealarm device : " + deviceIdentifier;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        List<Device> devices;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Fetching the details of all Virtual Firealarm devices");
            }
            devices = virtualFireAlarmDAO.getDeviceDAO().getAllDevices();
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error while fetching all Virtual Firealarm devices.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return devices;
    }

}