package org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.impl.dao.DroneAnalyzerDAO;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dao.IotDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dao.IotDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dto.IotDevice;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.util.IotDeviceManagementUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by geesara on 12/9/15.
 */
public class DroneAnalyzerManager implements DeviceManager {

    private static final IotDeviceManagementDAOFactory iotDeviceManagementDAOFactory = new DroneAnalyzerDAO();
    private static final Log log = LogFactory.getLog(DroneAnalyzerManager.class);

    @Override
    public FeatureManager getFeatureManager() {
        return null;
    }

    @Override
    public boolean saveConfiguration(TenantConfiguration tenantConfiguration)
            throws DeviceManagementException {
        //TODO implement this
        return false;
    }

    @Override
    public TenantConfiguration getConfiguration() throws DeviceManagementException {
        //TODO implement this
        return null;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        boolean status;
        IotDevice iotDevice = IotDeviceManagementUtil.convertToIotDevice(device);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Enrolling a new Virtual Firealarm device : " + device.getDeviceIdentifier());
            }
            DroneAnalyzerDAO.beginTransaction();
            status = iotDeviceManagementDAOFactory.getIotDeviceDAO().addIotDevice(
                    iotDevice);
            DroneAnalyzerDAO.commitTransaction();
        } catch (IotDeviceManagementDAOException e) {
            try {
                DroneAnalyzerDAO.rollbackTransaction();
            } catch (IotDeviceManagementDAOException iotDAOEx) {
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
        IotDevice iotDevice = IotDeviceManagementUtil.convertToIotDevice(device);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Modifying the Virtual Firealarm device enrollment data");
            }
            DroneAnalyzerDAO.beginTransaction();
            status = iotDeviceManagementDAOFactory.getIotDeviceDAO()
                    .updateIotDevice(iotDevice);
            DroneAnalyzerDAO.commitTransaction();
        } catch (IotDeviceManagementDAOException e) {
            try {
                DroneAnalyzerDAO.rollbackTransaction();
            } catch (IotDeviceManagementDAOException iotDAOEx) {
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
            DroneAnalyzerDAO.beginTransaction();
            status = iotDeviceManagementDAOFactory.getIotDeviceDAO()
                    .deleteIotDevice(deviceId.getId());
            DroneAnalyzerDAO.commitTransaction();
        } catch (IotDeviceManagementDAOException e) {
            try {
                DroneAnalyzerDAO.rollbackTransaction();
            } catch (IotDeviceManagementDAOException iotDAOEx) {
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
            IotDevice iotDevice = iotDeviceManagementDAOFactory.getIotDeviceDAO().getIotDevice(deviceId.getId());
            if (iotDevice != null) {
                isEnrolled = true;
            }
        } catch (IotDeviceManagementDAOException e) {
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
            IotDevice iotDevice = iotDeviceManagementDAOFactory.getIotDeviceDAO().
                    getIotDevice(deviceId.getId());
            device = IotDeviceManagementUtil.convertToDevice(iotDevice);
        } catch (IotDeviceManagementDAOException e) {
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
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException {
        boolean status;
        IotDevice iotDevice = IotDeviceManagementUtil.convertToIotDevice(device);
        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "updating the details of Virtual Firealarm device : " + deviceIdentifier);
            }
            DroneAnalyzerDAO.beginTransaction();
            status = iotDeviceManagementDAOFactory.getIotDeviceDAO()
                    .updateIotDevice(iotDevice);
            DroneAnalyzerDAO.commitTransaction();
        } catch (IotDeviceManagementDAOException e) {
            try {
                DroneAnalyzerDAO.rollbackTransaction();
            } catch (IotDeviceManagementDAOException iotDAOEx) {
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
        List<Device> devices = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Fetching the details of all Virtual Firealarm devices");
            }
            List<IotDevice> iotDevices =
                    iotDeviceManagementDAOFactory.getIotDeviceDAO().getAllIotDevices();
            if (iotDevices != null) {
                devices = new ArrayList<Device>();
                for (IotDevice iotDevice : iotDevices) {
                    devices.add(IotDeviceManagementUtil.convertToDevice(iotDevice));
                }
            }
        } catch (IotDeviceManagementDAOException e) {
            String msg = "Error while fetching all Virtual Firealarm devices.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return devices;
    }


    @Override
    public boolean requireDeviceAuthorization() {
        return true;
    }
}
