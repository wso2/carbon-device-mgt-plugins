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

package org.wso2.carbon.device.mgt.iot.controlqueue.mqtt;

import org.wso2.carbon.device.mgt.iot.config.server.DeviceManagementConfigurationManager;
import org.wso2.carbon.device.mgt.iot.config.server.datasource.ControlQueue;

public class MqttConfig {
	private String mqttQueueEndpoint;
	private String mqttQueueUsername;
	private String mqttQueuePassword;
	private boolean isEnabled;

	private static final String MQTT_QUEUE_CONFIG_NAME = "MQTT";
	private static final String LOCALHOST = "localhost";
	private static final String PORT_OFFSET_PROPERTY = "portOffset";

	private ControlQueue mqttControlQueue;

	private static MqttConfig mqttConfig = new MqttConfig();

	public String getMqttQueueEndpoint() {
		return mqttQueueEndpoint;
	}

	public String getMqttQueueUsername() {
		return mqttQueueUsername;
	}

	public String getMqttQueuePassword() {
		return mqttQueuePassword;
	}

	public ControlQueue getMqttControlQueue() {
		return mqttControlQueue;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public static String getMqttQueueConfigName() {
		return MQTT_QUEUE_CONFIG_NAME;
	}

	private MqttConfig() {
		mqttControlQueue = DeviceManagementConfigurationManager.getInstance().getControlQueue(MQTT_QUEUE_CONFIG_NAME);

		int portOffset = Integer.parseInt(System.getProperty(PORT_OFFSET_PROPERTY));
		String brokerURL = mqttControlQueue.getServerURL();


		if (portOffset != 0 && brokerURL.contains(LOCALHOST)) {
			// if using the internal MB (meaning URL is localhost and there is a portOffset)
			//          then increment port accordingly
			int mqttPort = mqttControlQueue.getPort();
			mqttPort = mqttPort + portOffset;
			mqttQueueEndpoint = mqttControlQueue.getServerURL() + ":" + mqttPort;

		} else {
			mqttQueueEndpoint = mqttControlQueue.getServerURL() + ":" + mqttControlQueue.getPort();
		}

		mqttQueueUsername = mqttControlQueue.getUsername();
		mqttQueuePassword = mqttControlQueue.getPassword();
		isEnabled = mqttControlQueue.isEnabled();
	}


	public static MqttConfig getInstance() {
		return mqttConfig;
	}

}
