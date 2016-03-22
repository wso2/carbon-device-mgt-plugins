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

package org.wso2.carbon.device.mgt.iot.arduino.controller.service.impl.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.constants.ArduinoConstants;
import org.wso2.carbon.device.mgt.iot.arduino.controller.service.impl.ArduinoControllerService;
import org.wso2.carbon.device.mgt.iot.config.server.DeviceManagementConfigurationManager;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.transport.mqtt.MQTTTransportHandler;

import java.io.File;
import java.util.LinkedList;
import java.util.UUID;

public class ArduinoMQTTConnector extends MQTTTransportHandler {
    private static Log log = LogFactory.getLog(ArduinoMQTTConnector.class);
    private static final String subscribeTopic = "wso2/" + ArduinoConstants.DEVICE_TYPE + "/#";
    private static final String iotServerSubscriber = UUID.randomUUID().toString().substring(0, 5);

    private static final String MESSAGE_TO_SEND = "IN";
    private static final String MESSAGE_RECEIVED = "OUT";

    private ArduinoMQTTConnector() {
        super(iotServerSubscriber, ArduinoConstants.DEVICE_TYPE,
              MqttConfig.getInstance().getMqttQueueEndpoint(), subscribeTopic);
    }

    @Override
    public void connect() {
        Runnable connector = new Runnable() {
            public void run() {
                while (!isConnected()) {
                    try {
                        String brokerUsername = MqttConfig.getInstance().getMqttQueueUsername();
                        String brokerPassword = MqttConfig.getInstance().getMqttQueuePassword();
                        setUsernameAndPassword(brokerUsername, brokerPassword);
                        connectToQueue();
                    } catch (TransportHandlerException e) {
                        log.error("Connection to MQTT Broker at: " + mqttBrokerEndPoint + " failed", e);
                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException ex) {
                            log.error("MQTT-Connector: Thread Sleep Interrupt Exception.", ex);
                        }
                    }

                    try {
                        subscribeToQueue();
                    } catch (TransportHandlerException e) {
                        log.warn("Subscription to MQTT Broker at: " + mqttBrokerEndPoint + " failed", e);
                    }
                }
            }
        };


        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

    @Override
    public void processIncomingMessage(MqttMessage message, String... messageParams) throws TransportHandlerException {
        if(messageParams.length != 0) {
            // owner and the deviceId are extracted from the MQTT topic to which the message was received.
            // <Topic> = [ServerName/Owner/DeviceType/DeviceId]
            String topic = messageParams[0];
            String[] topicParams = topic.split("/");
            String deviceId = topicParams[3];

            if (log.isDebugEnabled()) {
                log.debug("Received MQTT message for: [DEVICE.ID-" + deviceId + "]");
            }

            int lastIndex = message.toString().lastIndexOf(":");
            String msgContext = message.toString().substring(lastIndex + 1);

            LinkedList<String> deviceControlList;
            LinkedList<String> replyMessageList;

            if (msgContext.equals(MESSAGE_TO_SEND) || msgContext.equals(ArduinoConstants.STATE_ON) || msgContext.equals(
                    ArduinoConstants.STATE_OFF)) {

                if (log.isDebugEnabled()) {
                    log.debug("Received a control message: ");
                    log.debug("Control message topic: " + topic);
                    log.debug("Control message: " + message.toString());
                }

                synchronized (ArduinoControllerService.getInternalControlsQueue()) {
                    deviceControlList = ArduinoControllerService.getInternalControlsQueue().get(deviceId);
                    if (deviceControlList == null) {
                        ArduinoControllerService.getInternalControlsQueue()
                                .put(deviceId, deviceControlList = new LinkedList<String>());
                    }
                }
                deviceControlList.add(message.toString());

            } else if (msgContext.equals(MESSAGE_RECEIVED)) {

                if (log.isDebugEnabled()) {
                    log.debug("Received reply from a device: ");
                    log.debug("Reply message topic: " + topic);
                    log.debug("Reply message: " + message.toString().substring(0, lastIndex));
                }

                synchronized (ArduinoControllerService.getReplyMsgQueue()) {
                    replyMessageList = ArduinoControllerService.getReplyMsgQueue().get(deviceId);
                    if (replyMessageList == null) {
                        ArduinoControllerService.getReplyMsgQueue()
                                .put(deviceId, replyMessageList = new LinkedList<>());
                    }
                }
                replyMessageList.add(message.toString());
            }
        }
    }

    @Override
    public void disconnect() {
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {
                    try {
                        closeConnection();
                    } catch (MqttException e) {
                        if (log.isDebugEnabled()) {
                            log.warn("Unable to 'STOP' MQTT connection at broker at: " + mqttBrokerEndPoint
                                             + " for device-type - " + ArduinoConstants.DEVICE_TYPE, e);
                        }

                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException e1) {
                            log.error("MQTT-Terminator: Thread Sleep Interrupt Exception at device-type - " +
                                              ArduinoConstants.DEVICE_TYPE, e1);
                        }
                    }
                }
            }
        };

        Thread terminatorThread = new Thread(stopConnection);
        terminatorThread.setDaemon(true);
        terminatorThread.start();
    }

    @Override
    public void processIncomingMessage() throws TransportHandlerException {

    }

    @Override
    public void processIncomingMessage(MqttMessage message) throws TransportHandlerException {

    }

    @Override
    public void publishDeviceData() throws TransportHandlerException {

    }

    @Override
    public void publishDeviceData(MqttMessage publishData) throws TransportHandlerException {

    }

    @Override
    public void publishDeviceData(String... publishData) throws TransportHandlerException {

    }
}
