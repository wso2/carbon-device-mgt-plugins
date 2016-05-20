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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.application.extension.constants.ApiApplicationConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.util.Utils;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.xmpp.XmppConfig;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is used to create a zip file that includes the necessary configuration required for the agent.
 */
public class ZipUtil {

    private static final Log log = LogFactory.getLog(ZipUtil.class);
    private static final String HTTPS_PORT_PROPERTY = "httpsPort";
    private static final String HTTP_PORT_PROPERTY = "httpPort";

    private static final String LOCALHOST = "localhost";
    private static final String HTTPS_PROTOCOL_APPENDER = "https://";
    private static final String HTTP_PROTOCOL_APPENDER = "http://";

    public ZipArchive createZipFile(String owner, String deviceType, String deviceId, String deviceName,
                                    String apiApplicationKey, String token, String refreshToken)
            throws DeviceManagementException {

        String sketchFolder = "repository" + File.separator + "resources" + File.separator + "sketches";
        String archivesPath =
                CarbonUtils.getCarbonHome() + File.separator + sketchFolder + File.separator + "archives" +
                        File.separator + deviceId;
        String templateSketchPath = sketchFolder + File.separator + deviceType;
        String iotServerIP;

        try {
            iotServerIP = Utils.getServerUrl();
            String httpsServerPort = System.getProperty(HTTPS_PORT_PROPERTY);
            String httpServerPort = System.getProperty(HTTP_PORT_PROPERTY);
            String httpsServerEP = HTTPS_PROTOCOL_APPENDER + iotServerIP + ":" + httpsServerPort;
            String httpServerEP = HTTP_PROTOCOL_APPENDER + iotServerIP + ":" + httpServerPort;
            String apimEndpoint = httpsServerEP;
            String mqttEndpoint = MqttConfig.getInstance().getBrokerEndpoint();
            if (mqttEndpoint.contains(LOCALHOST)) {
                mqttEndpoint = mqttEndpoint.replace(LOCALHOST, iotServerIP);
            }

            String xmppEndpoint = XmppConfig.getInstance().getXmppServerIP() + ":" +
                    XmppConfig.getInstance().getXmppServerPort();
            if (xmppEndpoint.contains(LOCALHOST)) {
                xmppEndpoint = xmppEndpoint.replace(LOCALHOST, iotServerIP);
            }

            String base64EncodedApplicationKey = getBase64EncodedAPIAppKey(apiApplicationKey);

            Map<String, String> contextParams = new HashMap<>();
            contextParams.put(VirtualFireAlarmUtilConstants.TENANT_DOMAIN, APIUtil.getTenantDomainOftheUser());
            contextParams.put(VirtualFireAlarmUtilConstants.DEVICE_OWNER, owner);
            contextParams.put(VirtualFireAlarmUtilConstants.DEVICE_ID, deviceId);
            contextParams.put(VirtualFireAlarmUtilConstants.DEVICE_NAME, deviceName);
            contextParams.put(VirtualFireAlarmUtilConstants.HTTPS_EP, httpsServerEP);
            contextParams.put(VirtualFireAlarmUtilConstants.HTTP_EP, httpServerEP);
            contextParams.put(VirtualFireAlarmUtilConstants.APIM_EP, apimEndpoint);
            contextParams.put(VirtualFireAlarmUtilConstants.MQTT_EP, mqttEndpoint);
            contextParams.put(VirtualFireAlarmUtilConstants.XMPP_EP, "XMPP:" + xmppEndpoint);
            contextParams.put(VirtualFireAlarmUtilConstants.API_APPLICATION_KEY, base64EncodedApplicationKey);
            contextParams.put(VirtualFireAlarmUtilConstants.DEVICE_TOKEN, token);
            contextParams.put(VirtualFireAlarmUtilConstants.DEVICE_REFRESH_TOKEN, refreshToken);
            contextParams.put(VirtualFireAlarmUtilConstants.SERVER_NAME, XmppConfig.getInstance().getXmppServerName());
            ZipArchive zipFile;
            zipFile = Utils.getSketchArchive(archivesPath, templateSketchPath, contextParams, deviceName);
            return zipFile;
        } catch (IOException e) {
            throw new DeviceManagementException("Zip File Creation Failed", e);
        }
    }

    private String getBase64EncodedAPIAppKey(String apiAppCredentialsAsJSONString) {

        JSONObject jsonObject = new JSONObject(apiAppCredentialsAsJSONString);
        String consumerKey = jsonObject.get(ApiApplicationConstants.OAUTH_CLIENT_ID).toString();
        String consumerSecret = jsonObject.get(ApiApplicationConstants.OAUTH_CLIENT_SECRET).toString();
        String stringToEncode = consumerKey + ":" + consumerSecret;
        return Base64.encodeBase64String(stringToEncode.getBytes());
    }
}
