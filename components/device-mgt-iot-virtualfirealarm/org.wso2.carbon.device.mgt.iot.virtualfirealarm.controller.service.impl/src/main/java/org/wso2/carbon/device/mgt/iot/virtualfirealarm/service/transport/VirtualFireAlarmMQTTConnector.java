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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.iot.config.server.DeviceManagementConfigurationManager;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.transport.mqtt.MQTTTransportHandler;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.exception.VirtualFireAlarmException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.util.SecurityManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.util.VirtualFireAlarmServiceUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.UUID;

/**
 * This is an example for the use of the MQTT capabilities provided by the IoT-Server. This example depicts the use
 * of MQTT Transport for the VirtualFirealarm device-type. This class extends the abstract class
 * "MQTTTransportHandler". "MQTTTransportHandler" consists of the MQTT client specific functionality and implements
 * the "TransportHandler" interface. The actual functionality related to the "TransportHandler" interface is
 * implemented here, in this concrete class. Whilst the abstract class "MQTTTransportHandler" is intended to provide
 * the common MQTT functionality, this class (which is its extension) provides the implementation specific to the
 * MQTT communication of the Device-Type (VirtualFirealarm) in concern.
 * <p/>
 * Hence, the methods of this class are implementation of the "TransportHandler" interface which handles the device
 * specific logic to connect-to, publish-to, process-incoming-messages-from and disconnect-from the MQTT broker
 * listed in the configurations.
 */
@SuppressWarnings("no JAX-WS annotation")
public class VirtualFireAlarmMQTTConnector extends MQTTTransportHandler {
    private static Log log = LogFactory.getLog(VirtualFireAlarmMQTTConnector.class);

    // the server name is read from the IoT-Server config file `devicemgt-config.xml`.
    // it is used as a prefix to the MQTT-Topic.
    private static String serverName = DeviceManagementConfigurationManager.getInstance().
            getDeviceManagementServerInfo().getName();

    // subscription topic: <SERVER_NAME>/+/virtual_firealarm/+/publisher
    // wildcard (+) is in place for device_owner & device_id
    private static String subscribeTopic = serverName + File.separator + "+" + File.separator +
            VirtualFireAlarmConstants.DEVICE_TYPE + File.separator + "+" + File.separator + "publisher";

    private static String iotServerSubscriber = UUID.randomUUID().toString().substring(0, 5);

    /**
     * Default constructor for the VirtualFirealarmMQTTConnector.
     */
    private VirtualFireAlarmMQTTConnector() {
        super(iotServerSubscriber, VirtualFireAlarmConstants.DEVICE_TYPE,
              MqttConfig.getInstance().getMqttQueueEndpoint(), subscribeTopic);
    }

    /**
     * {@inheritDoc}
     * VirtualFirealarm device-type specific implementation to connect to the MQTT broker and subscribe to a topic.
     * This method is called to initiate a MQTT communication.
     */
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

    /**
     * {@inheritDoc}
     * VirtualFirealarm device-type specific implementation to process incoming messages. This is the specific
     * method signature of the overloaded "processIncomingMessage" method that gets called from the messageArrived()
     * callback of the "MQTTTransportHandler".
     */
    @Override
    public void processIncomingMessage(MqttMessage mqttMessage, String... messageParams) {
        String topic = messageParams[0];
        // owner and the deviceId are extracted from the MQTT topic to which the messgae was received.
        String ownerAndId = topic.replace(serverName + File.separator, "");
        ownerAndId = ownerAndId.replace(File.separator + VirtualFireAlarmConstants.DEVICE_TYPE + File.separator, ":");
        ownerAndId = ownerAndId.replace(File.separator + "publisher", "");

        String owner = ownerAndId.split(":")[0];
        String deviceId = ownerAndId.split(":")[1];

        if (log.isDebugEnabled()) {
            log.debug("Received MQTT message for: [OWNER-" + owner + "] & [DEVICE.ID-" + deviceId + "]");
        }

        String actualMessage;
        try {
            // the hash-code of the deviceId is used as the alias for device certificates during SCEP enrollment.
            // hence, the same is used here to fetch the device-specific-certificate from the key store.
            PublicKey clientPublicKey = VirtualFireAlarmServiceUtils.getDevicePublicKey(deviceId);
            PrivateKey serverPrivateKey = SecurityManager.getServerPrivateKey();

            // the MQTT-messages from VirtualFireAlarm devices are in the form {"Msg":<MESSAGE>, "Sig":<SIGNATURE>}
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
                SensorDataManager.getInstance().setSensorRecord(deviceId, VirtualFireAlarmConstants.SENSOR_TEMP,
                                                                temperatureValue,
                                                                Calendar.getInstance().getTimeInMillis());
            }
        } catch (VirtualFireAlarmException e) {
            String errorMsg =
                    "CertificateManagementService failure oo Signature-Verification/Decryption was unsuccessful.";
            log.error(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     * VirtualFirealarm device-type specific implementation to publish data to the device. This method calls the
     * {@link #publishToQueue(String, MqttMessage)} method of the "MQTTTransportHandler" class.
     */
    @Override
    public void publishDeviceData(String... publishData) throws TransportHandlerException {
        if (publishData.length != 4) {
            String errorMsg = "Incorrect number of arguments received to SEND-MQTT Message. " +
                    "Need to be [owner, deviceId, resource{BULB/TEMP}, state{ON/OFF or null}]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg);
        }

        String deviceOwner = publishData[0];
        String deviceId = publishData[1];
        String resource = publishData[2];
        String state = publishData[3];

        MqttMessage pushMessage = new MqttMessage();
        String publishTopic =
                serverName + File.separator + deviceOwner + File.separator +
                        VirtualFireAlarmConstants.DEVICE_TYPE + File.separator + deviceId;

        try {
            PublicKey devicePublicKey = VirtualFireAlarmServiceUtils.getDevicePublicKey(deviceId);
            PrivateKey serverPrivateKey = SecurityManager.getServerPrivateKey();

            String actualMessage = resource + ":" + state;
            String encryptedMsg = VirtualFireAlarmServiceUtils.prepareSecurePayLoad(actualMessage,
                                                                                    devicePublicKey,
                                                                                    serverPrivateKey);

            pushMessage.setPayload(encryptedMsg.getBytes(StandardCharsets.UTF_8));
            pushMessage.setQos(DEFAULT_MQTT_QUALITY_OF_SERVICE);
            pushMessage.setRetained(false);

            publishToQueue(publishTopic, pushMessage);

        } catch (VirtualFireAlarmException e) {
            String errorMsg = "Preparing Secure payload failed for device - [" + deviceId + "] of owner - " +
                    "[" + deviceOwner + "].";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        }
    }


    /**
     * {@inheritDoc}
     * VirtualFirealarm device-type specific implementation to disconnect from the MQTT broker.
     */
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
                                             + " for device-type - " + VirtualFireAlarmConstants.DEVICE_TYPE, e);
                        }

                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException e1) {
                            log.error("MQTT-Terminator: Thread Sleep Interrupt Exception at device-type - " +
                                              VirtualFireAlarmConstants.DEVICE_TYPE, e1);
                        }
                    }
                }
            }
        };

        Thread terminatorThread = new Thread(stopConnection);
        terminatorThread.setDaemon(true);
        terminatorThread.start();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void publishDeviceData() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishDeviceData(MqttMessage publishData) throws TransportHandlerException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processIncomingMessage() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processIncomingMessage(MqttMessage message) throws TransportHandlerException {
        // nothing to do
    }
}
