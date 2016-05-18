/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.internal.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.internal.config.exception.InvalidConfigurationStateException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.internal.config.exception.VirtualFireAlarmConfigurationException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.internal.util.VirtualFireAlarmUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@XmlRootElement(name = "DeviceManagementConfiguration")
public class VirtualFireAlarmConfig {

    private DeviceManagementConfigRepository deviceManagementConfigRepository;
    private PushNotificationConfig pushNotificationConfig;
    private static VirtualFireAlarmConfig config;

    private static final Log log = LogFactory.getLog(VirtualFireAlarmConfig.class);
    private static final String VIRTUAL_FIRE_ALARM_CONFIG_PATH =
            CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + "device-mgt-plugins" + File.separator +
                    "virtual-fire-alarm" + File.separator + "virtual-fire-alarm-config.xml";

    private VirtualFireAlarmConfig() {
    }

    public static VirtualFireAlarmConfig getInstance() {
        if (config == null) {
            throw new InvalidConfigurationStateException("Webapp Authenticator Configuration is not " +
                    "initialized properly");
        }
        return config;
    }

    @XmlElement(name = "ManagementRepository", required = true)
    public DeviceManagementConfigRepository getDeviceManagementConfigRepository() {
        return deviceManagementConfigRepository;
    }

    public void setDeviceManagementConfigRepository(DeviceManagementConfigRepository deviceManagementConfigRepository) {
        this.deviceManagementConfigRepository = deviceManagementConfigRepository;
    }

    @XmlElement(name = "PushNotificationConfiguration", required = false)
    public PushNotificationConfig getPushNotificationConfig() {
        return pushNotificationConfig;
    }

    public void setPushNotificationConfig(PushNotificationConfig pushNotificationConfig) {
        this.pushNotificationConfig = pushNotificationConfig;
    }

    public static void init() throws VirtualFireAlarmConfigurationException {
        try {
            File authConfig = new File(VirtualFireAlarmConfig.VIRTUAL_FIRE_ALARM_CONFIG_PATH);
            Document doc = VirtualFireAlarmUtil.convertToDocument(authConfig);

            /* Un-marshaling Webapp Authenticator configuration */
            JAXBContext ctx = JAXBContext.newInstance(VirtualFireAlarmConfig.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            //unmarshaller.setSchema(getSchema());
            config = (VirtualFireAlarmConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new VirtualFireAlarmConfigurationException("Error occurred while un-marshalling Virtual Fire Alarm " +
                    " Config", e);
        }
    }

}
