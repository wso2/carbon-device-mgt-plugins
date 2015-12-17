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

package org.wso2.carbon.device.mgt.iot.util;

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.config.server.DeviceManagementConfigurationManager;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.util.IotDeviceManagementUtil;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ZipUtil {

	public ZipArchive downloadSketch(String owner, String tenantDomain, String deviceType,
	                                 String deviceId, String deviceName, String token,
	                                 String refreshToken)
			throws DeviceManagementException {

		if (owner == null || deviceType == null) {
			throw new DeviceManagementException("Invalid parameters for `owner` or `deviceType`");
		}

		String sep = File.separator;
		String sketchFolder = "repository" + sep + "resources" + sep + "sketches";
		String archivesPath = CarbonUtils.getCarbonHome() + sep + sketchFolder + sep + "archives"
				+ sep + deviceId;
		String templateSketchPath = sketchFolder + sep + deviceType;

		String iotServerIP = System.getProperty("carbon.local.ip");     // bind.address
		String httpsServerPort = System.getProperty("httpsPort");
		String httpServerPort = System.getProperty("httpPort");

		String httpsServerEP = "https://" + iotServerIP + ":" + httpsServerPort;
		String httpServerEP = "http://" + iotServerIP + ":" + httpServerPort;

		String apimHost =
				DeviceManagementConfigurationManager.getInstance().getDeviceCloudMgtConfig().getApiManager()
						.getServerURL();

		String apimGatewayPort =
				DeviceManagementConfigurationManager.getInstance().getDeviceCloudMgtConfig().getApiManager()
						.getGatewayPort();

		String apimEndpoint = apimHost + ":" + apimGatewayPort;
		String mqttEndpoint = MqttConfig.getInstance().getMqttQueueEndpoint();
		String xmppEndpoint = XmppConfig.getInstance().getXmppEndpoint();

		int indexOfChar = xmppEndpoint.lastIndexOf(":");
		if (indexOfChar != -1) {
			xmppEndpoint = xmppEndpoint.substring(0, indexOfChar);
		}

		xmppEndpoint = xmppEndpoint + ":" + XmppConfig.getInstance().getSERVER_CONNECTION_PORT();

		Map<String, String> contextParams = new HashMap<String, String>();
		contextParams.put("DEVICE_OWNER", owner);
		contextParams.put("DEVICE_ID", deviceId);
		contextParams.put("DEVICE_NAME", deviceName);
		contextParams.put("HTTPS_EP", httpsServerEP);
		contextParams.put("HTTP_EP", httpServerEP);
		contextParams.put("APIM_EP", apimEndpoint);
		contextParams.put("MQTT_EP", mqttEndpoint);
		contextParams.put("XMPP_EP", xmppEndpoint);
		contextParams.put("DEVICE_TOKEN", token);
		contextParams.put("DEVICE_REFRESH_TOKEN", refreshToken);

		ZipArchive zipFile;
		try {
			zipFile = IotDeviceManagementUtil.getSketchArchive(archivesPath, templateSketchPath, contextParams);
		} catch (IOException e) {
			throw new DeviceManagementException("Zip File Creation Failed", e);
		}

		return zipFile;
	}
}
