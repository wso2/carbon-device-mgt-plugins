/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.arduino.service.impl.util;

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.iot.util.Utils;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is used to create a zip file that includes the necessary configuration required for the agent.
 */
public class ZipUtil {

    private static final String HTTP_PORT_PROPERTY = "httpPort";
    private static final String CONFIG_TYPE = "general";

    public ZipArchive createZipFile(String owner, String tenantDomain, String deviceType,
                                    String deviceId, String deviceName, String token,
                                    String refreshToken) throws DeviceManagementException {

        String sketchFolder = "repository" + File.separator + "resources" + File.separator + "sketches";
        String archivesPath = CarbonUtils.getCarbonHome() + File.separator + sketchFolder + File.separator + "archives" +
                        File.separator + deviceId;
        String templateSketchPath = sketchFolder + File.separator + deviceType;
        String iotServerIP;

        try {
            iotServerIP = Utils.getServerUrl();
            String httpServerPort = System.getProperty(HTTP_PORT_PROPERTY);

            Map<String, String> contextParams = new HashMap<>();

            if (APIUtil.getTenantDomainOftheUser().equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                contextParams.put("TENANT_DOMAIN", "");
            } else {
                contextParams.put("TENANT_DOMAIN", "/t/" + tenantDomain);
            }
            PlatformConfiguration configuration = APIUtil.getTenantConfigurationManagementService().getConfiguration(
                    CONFIG_TYPE);
            if (configuration != null && configuration.getConfiguration() != null && configuration
                    .getConfiguration().size() > 0) {
                List<ConfigurationEntry> configurations = configuration.getConfiguration();
                for (ConfigurationEntry configurationEntry : configurations) {
                    switch (configurationEntry.getName()) {
                        case "ARDUINO_HTTP_IP":
                            iotServerIP = (String)configurationEntry.getValue();
                            break;
                        case "ARDUINO_HTTP_PORT":
                            httpServerPort = (String)configurationEntry.getValue();
                            break;
                    }
                }
            }

            contextParams.put("DEVICE_OWNER", owner);
            contextParams.put("DEVICE_ID", deviceId);
            contextParams.put("DEVICE_NAME", deviceName);
            contextParams.put("SERVER_EP_IP", iotServerIP.replace('.', ','));
            contextParams.put("SERVER_EP_PORT", httpServerPort);
            contextParams.put("DEVICE_TOKEN", token);
            contextParams.put("DEVICE_REFRESH_TOKEN", refreshToken);

            ZipArchive zipFile;
            zipFile = Utils.getSketchArchive(archivesPath, templateSketchPath, contextParams, deviceName);
            return zipFile;
        } catch (IOException e) {
            throw new DeviceManagementException("Zip File Creation Failed", e);
        } catch (ConfigurationManagementException e) {
            throw new DeviceManagementException("Failed to retrieve configuration", e);
        }
    }
}
