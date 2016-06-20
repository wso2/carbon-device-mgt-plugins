package org.wso2.carbon.device.mgt.iot.devicetype;

import org.wso2.carbon.device.mgt.iot.devicetype.config.DeviceManagementConfiguration;

/**
 * Service to retrieve device type configs.
 */
public interface DeviceTypeConfigService {

    /**
     * This service will read the device type configuration files from conf/etc/device-type-plugins
     *
     * @param deviceType retrive the device type configuration.
     * @param tenantDomain retrieve the device type of this tenant domain.
     * @return device management configuratio for the device type owned by the given  tenant domain.
     */
    DeviceManagementConfiguration getConfiguration(String deviceType, String tenantDomain);
}
