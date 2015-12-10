package org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.impl;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;

import java.util.List;

/**
 * Created by geesara on 12/9/15.
 */
public class DroneAnalyzerManagerService implements DeviceManagementService {
    private DeviceManager deviceManager;
    @Override
    public String getType() {
        return DroneConstants.DEVICE_TYPE;
    }

    @Override
    public void init() throws DeviceManagementException {
        this.deviceManager = new DroneAnalyzerManager();
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
    public void notifyOperationToDevices(Operation operation, List<DeviceIdentifier> list)
            throws DeviceManagementException {

    }

    @Override
    public Application[] getApplications(String s, int i, int i1) throws ApplicationManagementException {
        return new Application[0];
    }

    @Override
    public void updateApplicationStatus(DeviceIdentifier deviceIdentifier, Application application, String s)
            throws ApplicationManagementException {

    }

    @Override
    public String getApplicationStatus(DeviceIdentifier deviceIdentifier, Application application)
            throws ApplicationManagementException {
        return null;
    }

    @Override
    public void installApplicationForDevices(Operation operation, List<DeviceIdentifier> list)
            throws ApplicationManagementException {

    }

    @Override
    public void installApplicationForUsers(Operation operation, List<String> list)
            throws ApplicationManagementException {

    }

    @Override
    public void installApplicationForUserRoles(Operation operation, List<String> list)
            throws ApplicationManagementException {

    }

    public String getProviderTenantDomain() {
        return "carbon.super";
    }

    public boolean isSharedWithAllTenants() {
        return true;
    }

    public String[] getSharedTenantsDomain() {
        return new String[0];
    }
}
