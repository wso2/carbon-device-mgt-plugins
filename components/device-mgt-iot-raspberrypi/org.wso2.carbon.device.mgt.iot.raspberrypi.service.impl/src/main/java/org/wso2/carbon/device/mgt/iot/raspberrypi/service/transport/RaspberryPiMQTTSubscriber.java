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

package org.wso2.carbon.device.mgt.iot.raspberrypi.service.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.config.server.DeviceManagementConfigurationManager;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttSubscriber;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.constants.RaspberrypiConstants;
import org.wso2.carbon.device.mgt.iot.raspberrypi.service.util.RaspberrypiServiceUtils;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;

public class RaspberryPiMQTTSubscriber extends MqttSubscriber {
    private static Log log = LogFactory.getLog(RaspberryPiMQTTSubscriber.class);

    private static final String serverName =
            DeviceManagementConfigurationManager.getInstance().getDeviceManagementServerInfo().getName();
    private static final String subscribeTopic =
            serverName + File.separator + "+" + File.separator + RaspberrypiConstants.DEVICE_TYPE +
                    File.separator + "+" + File.separator + "publisher";

    private static final String iotServerSubscriber = UUID.randomUUID().toString().substring(0, 5);
    private String mqttEndpoint;

    private RaspberryPiMQTTSubscriber() {
        super(iotServerSubscriber, RaspberrypiConstants.DEVICE_TYPE,
              MqttConfig.getInstance().getMqttQueueEndpoint(), subscribeTopic);
    }

    public void initConnector() {
        mqttEndpoint = MqttConfig.getInstance().getMqttQueueEndpoint();
    }

    public void connectAndSubscribe() {
        try {
            super.connectAndSubscribe();
        } catch (DeviceManagementException e) {
            log.error("Subscription to MQTT Broker at: " + mqttEndpoint + " failed");
            retryMQTTSubscription();
        }
    }

    @Override
    protected void postMessageArrived(String topic, MqttMessage mqttMessage) {
        String ownerAndId = topic.replace("wso2" + File.separator + "iot" + File.separator, "");
        ownerAndId = ownerAndId.replace(File.separator + RaspberrypiConstants.DEVICE_TYPE + File.separator, ":");
        ownerAndId = ownerAndId.replace(File.separator + "publisher", "");

        String owner = ownerAndId.split(":")[0];
        String deviceId = ownerAndId.split(":")[1];
        String receivedMessage = mqttMessage.toString();

        if (log.isDebugEnabled()) {
            log.debug("Received MQTT message for: {OWNER-" + owner + "} & {DEVICE.ID-" + deviceId + "}");
            log.debug("MQTT: Received Message [" + receivedMessage + "] topic: [" + topic + "]");
        }

        if (receivedMessage.contains("PUBLISHER")) {
            float temperature = Float.parseFloat(receivedMessage.split(":")[2]);

            if (!RaspberrypiServiceUtils.publishToDAS(owner, deviceId, temperature)) {
                log.error("MQTT Subscriber: Publishing data to DAS failed.");
            }

            if (log.isDebugEnabled()) {
                log.debug("MQTT Subscriber: Published data to DAS successfully.");
            }

        } else if (receivedMessage.contains("TEMPERATURE")) {
            String temperatureValue = receivedMessage.split(":")[1];
            SensorDataManager.getInstance().setSensorRecord(deviceId, RaspberrypiConstants.SENSOR_TEMPERATURE,
                                                            temperatureValue,
                                                            Calendar.getInstance().getTimeInMillis());
        }
    }

    private void retryMQTTSubscription() {
        Thread retryToSubscribe = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (!isConnected()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Subscriber re-trying to reach MQTT queue....");
                        }

                        try {
                            RaspberryPiMQTTSubscriber.super.connectAndSubscribe();
                        } catch (DeviceManagementException e1) {
                            if (log.isDebugEnabled()) {
                                log.debug("Attempt to re-connect to MQTT-Queue failed");
                            }
                        }
                    } else {
                        break;
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        log.error("MQTT: Thread S;eep Interrupt Exception");
                    }
                }
            }
        };

        retryToSubscribe.setDaemon(true);
        retryToSubscribe.start();
    }
}

