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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttSubscriber;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.exception.VirtualFireAlarmException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.util.VerificationManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.util.VirtualFireAlarmServiceUtils;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.UUID;

public class VirtualFireAlarmMQTTSubscriber extends MqttSubscriber {
    private static Log log = LogFactory.getLog(VirtualFireAlarmMQTTSubscriber.class);

    private static final String subscribeTopic =
            "wso22" + File.separator + "iot" + File.separator + "+" + File.separator +
                    VirtualFireAlarmConstants.DEVICE_TYPE + File.separator + "+" + File.separator +
                    "publisher";

    private static final String iotServerSubscriber = UUID.randomUUID().toString().substring(0, 5);
    private static String mqttEndpoint;

    private VirtualFireAlarmMQTTSubscriber() {
        super(iotServerSubscriber, VirtualFireAlarmConstants.DEVICE_TYPE,
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
        ownerAndId = ownerAndId.replace(File.separator + VirtualFireAlarmConstants.DEVICE_TYPE + File.separator, ":");
        ownerAndId = ownerAndId.replace(File.separator + "publisher", "");

        String owner = ownerAndId.split(":")[0];
        String deviceId = ownerAndId.split(":")[1];

        if (log.isDebugEnabled()) {
            log.debug("Received MQTT message for: {OWNER-" + owner + "} & {DEVICE.ID-" + deviceId + "}");
        }

        String actualMessage = "";

        try {
            PublicKey clientPublicKey = VirtualFireAlarmServiceUtils.getDevicePublicKey(deviceId);
            PrivateKey serverPrivateKey = VerificationManager.getServerPrivateKey();
            actualMessage = VirtualFireAlarmServiceUtils.extractMessageFromPayload(mqttMessage.toString(),
                                                                                   serverPrivateKey, clientPublicKey);

            if (log.isDebugEnabled()) {
                log.debug("MQTT: Received Message [" + actualMessage + "] topic: [" + topic + "]");
            }

            if (actualMessage.contains("PUBLISHER")) {
                float temperature = Float.parseFloat(actualMessage.split(":")[2]);

                if (!VirtualFireAlarmServiceUtils.publishToDAS(owner, deviceId, temperature)) {
                    log.error("MQTT Subscriber: Publishing data to DAS failed.");
                }

                if (log.isDebugEnabled()) {
                    log.debug("MQTT Subscriber: Published data to DAS successfully.");
                }

            } else if (actualMessage.contains("TEMPERATURE")) {
                String temperatureValue = actualMessage.split(":")[1];
                SensorDataManager.getInstance().setSensorRecord(deviceId, VirtualFireAlarmConstants.SENSOR_TEMPERATURE,
                                                                temperatureValue,
                                                                Calendar.getInstance().getTimeInMillis());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("MQTT: Random Message [" + actualMessage + "] topic: [" + topic + "]");
                }
            }
        } catch (VirtualFireAlarmException e) {
            String errorMsg =
                    "CertificateManagementService failure oo Signature-Verification/Decryption was unsuccessful.";
            log.error(errorMsg, e);
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
                            VirtualFireAlarmMQTTSubscriber.super.connectAndSubscribe();
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
