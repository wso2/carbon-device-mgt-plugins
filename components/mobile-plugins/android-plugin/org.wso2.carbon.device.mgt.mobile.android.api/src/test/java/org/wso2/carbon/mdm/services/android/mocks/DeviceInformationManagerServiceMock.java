package org.wso2.carbon.mdm.services.android.mocks;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;

import java.util.List;

public class DeviceInformationManagerServiceMock implements DeviceInformationManager {
    @Override
    public void addDeviceInfo(DeviceIdentifier deviceIdentifier, DeviceInfo deviceInfo)
            throws DeviceDetailsMgtException {

    }

    @Override
    public DeviceInfo getDeviceInfo(DeviceIdentifier deviceIdentifier) throws DeviceDetailsMgtException {
        return null;
    }

    @Override
    public List<DeviceInfo> getDevicesInfo(List<DeviceIdentifier> list) throws DeviceDetailsMgtException {
        return null;
    }

    @Override
    public void addDeviceLocation(DeviceLocation deviceLocation) throws DeviceDetailsMgtException {

    }

    @Override
    public DeviceLocation getDeviceLocation(DeviceIdentifier deviceIdentifier) throws DeviceDetailsMgtException {
        return null;
    }

    @Override
    public List<DeviceLocation> getDeviceLocations(List<DeviceIdentifier> list) throws DeviceDetailsMgtException {
        return null;
    }
}
