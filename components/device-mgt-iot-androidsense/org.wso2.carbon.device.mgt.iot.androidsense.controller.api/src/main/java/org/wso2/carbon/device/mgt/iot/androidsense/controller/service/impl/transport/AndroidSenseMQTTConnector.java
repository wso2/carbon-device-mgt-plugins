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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.androidsense.controller.service.impl.transport;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.iot.androidsense.controller.service.impl.util.DeviceData;
import org.wso2.carbon.device.mgt.iot.androidsense.controller.service.impl.util.SensorData;
import org.wso2.carbon.device.mgt.iot.androidsense.plugin.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.config.server.DeviceManagementConfigurationManager;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.transport.mqtt.MQTTTransportHandler;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SuppressWarnings("no JAX-WS annotation")
public class AndroidSenseMQTTConnector extends MQTTTransportHandler {
    private static Log log = LogFactory.getLog(AndroidSenseMQTTConnector.class);
    private static String subscribeTopic = AndroidSenseConstants.MQTT_SUBSCRIBE_WORDS_TOPIC;
    private static String iotServerSubscriber = UUID.randomUUID().toString().substring(0, 5);

    private AndroidSenseMQTTConnector() {
        super(iotServerSubscriber, AndroidSenseConstants.DEVICE_TYPE,
              MqttConfig.getInstance().getMqttQueueEndpoint(), subscribeTopic);
    }

    @Override
    public void connect() {
        Runnable connector = new Runnable() {
            public void run() {
                while (!isConnected()) {
                    try {
                        connectToQueue();
                        subscribeToQueue();
                    } catch (TransportHandlerException e) {
                        log.warn("Connection/Subscription to MQTT Broker at: " + mqttBrokerEndPoint + " failed");
                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException ex) {
                            log.error("MQTT-Subscriber: Thread Sleep Interrupt Exception.", ex);
                        }
                    }
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

    /**
     * @throws TransportHandlerException in the event of any exceptions that occur whilst processing the message.
     */
    @Override
    public void processIncomingMessage() throws TransportHandlerException {
    }

    /**
     * @param message the message (of the type specific to the protocol) received from the device.
     * @throws TransportHandlerException
     */
    @Override
    public void processIncomingMessage(MqttMessage message) throws TransportHandlerException {
    }


    @Override
    public void publishDeviceData(String... publishData) throws TransportHandlerException {
        if (publishData.length != 3) {
            String errorMsg = "Incorrect number of arguments received to SEND-MQTT Message. " +
                    "Need to be [owner, deviceId, content]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg);
        }
        String deviceId = publishData[0];
        String operation = publishData[1];
        String resource = publishData[2];
        MqttMessage pushMessage = new MqttMessage();
        String publishTopic = "wso2/" + AndroidSenseConstants.DEVICE_TYPE + "/" + deviceId + "/command";
        if (operation.equals("add")) {
            publishTopic = publishTopic + "/words";
        } else if (operation.equals("remove")) {
            publishTopic = publishTopic + "/remove";
        } else if (operation.equals("threshold")) {
            publishTopic = publishTopic + "/threshold";
        } else {
            return;
        }
        String actualMessage = resource;
        pushMessage.setPayload(actualMessage.getBytes(StandardCharsets.UTF_8));
        pushMessage.setQos(DEFAULT_MQTT_QUALITY_OF_SERVICE);
        pushMessage.setRetained(false);
        publishToQueue(publishTopic, pushMessage);
    }

    @Override
    public void processIncomingMessage(MqttMessage mqttMessage, String... strings) throws TransportHandlerException {
        String[] topic = strings[0].split("/");
        String deviceId = topic[3];
        if (log.isDebugEnabled()) {
            log.debug("Received MQTT message for:  & [DEVICE.ID-" + deviceId + "]");
        }
        try {
            Gson gson = new Gson();
            String actualMessage = mqttMessage.toString();
            DeviceData deviceData = gson.fromJson(actualMessage, DeviceData.class);

            SensorData[] sensorData = deviceData.values;
            String streamDef = null;
            Object payloadData[] = null;
            String sensorName = null;
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            ctx.setTenantDomain("carbon.super", true);
            for (SensorData sensor : sensorData) {
                if (sensor.key.equals("battery")) {
                    streamDef = AndroidSenseConstants.BATTERY_STREAM_DEFINITION;
                    payloadData = new Float[]{Float.parseFloat(sensor.value)};
                    sensorName = AndroidSenseConstants.SENSOR_BATTERY;
                } else if (sensor.key.equals("GPS")) {
                    streamDef = AndroidSenseConstants.GPS_STREAM_DEFINITION;
                    String gpsValue = sensor.value;
                    String gpsValuesString[] = gpsValue.split(",");
                    Float gpsValues[] = new Float[2];
                    gpsValues[0] = Float.parseFloat(gpsValuesString[0]);
                    gpsValues[1] = Float.parseFloat(gpsValuesString[0]);
                    payloadData = gpsValues;
                    sensorName = AndroidSenseConstants.SENSOR_GPS;
                } else if (sensor.key.equals("word")) {
                    try {
                        streamDef = AndroidSenseConstants.WORD_COUNT_STREAM_DEFINITION;
                        String[] values = sensor.value.split(",");
                        String sessionId = values[0];
                        String keyword = values[1];
                        int occurrence = Integer.parseInt(values[2]);
                        String status = values[3];
                        sensorName = AndroidSenseConstants.SENSOR_WORDCOUNT;

                        if (occurrence > 0) {
                            payloadData = new Object[]{sessionId, keyword, status};
                            for (int i = 0; i < occurrence; i++) {
                                Long timestamp = Long.parseLong(values[3 + occurrence]);
                                publishDataToDAS(deviceId, timestamp, sensorName, streamDef,
                                                 sensor.value, payloadData);

                            }
                            continue;
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        log.error(
                                "Timestamp does not match the occurence sensor values are sent from the device.");
                    }
                    continue;
                } else {
                    try {
                        int androidSensorId = Integer.parseInt(sensor.key);
                        String sensorValue = sensor.value;
                        String sensorValuesString[] = sensorValue.split(",");
                        Float sensorValues[] = new Float[1];

                        switch (androidSensorId) {
                            case 1:
                                streamDef = AndroidSenseConstants.ACCELEROMETER_STREAM_DEFINITION;
                                sensorValues[0] = Float.parseFloat(sensorValuesString[0]) * Float.parseFloat(
                                        sensorValuesString[0]) * Float.
                                        parseFloat(sensorValuesString[0]);
                                payloadData = sensorValues;
                                sensorName = AndroidSenseConstants.SENSOR_ACCELEROMETER;
                                break;
                            case 2:
                                streamDef = AndroidSenseConstants.MAGNETIC_STREAM_DEFINITION;
                                sensorValues[0] = Float.parseFloat(sensorValuesString[0]) * Float.parseFloat(
                                        sensorValuesString[0]) * Float.
                                        parseFloat(sensorValuesString[0]);
                                payloadData = sensorValues;
                                sensorName = AndroidSenseConstants.SENSOR_MAGNETIC;
                                break;
                            case 4:
                                streamDef = AndroidSenseConstants.GYROSCOPE_STREAM_DEFINITION;
                                sensorValues[0] = Float.parseFloat(sensorValuesString[0]) * Float.parseFloat(
                                        sensorValuesString[0]) * Float.
                                        parseFloat(sensorValuesString[0]);
                                payloadData = sensorValues;
                                sensorName = AndroidSenseConstants.SENSOR_GYROSCOPE;
                                break;
                            case 5:
                                streamDef = AndroidSenseConstants.LIGHT_STREAM_DEFINITION;
                                sensorName = AndroidSenseConstants.SENSOR_LIGHT;
                                payloadData = new Float[]{Float.parseFloat(sensorValuesString[0])};
                                break;
                            case 6:
                                streamDef = AndroidSenseConstants.PRESSURE_STREAM_DEFINITION;
                                sensorName = AndroidSenseConstants.SENSOR_PRESSURE;
                                payloadData = new Float[]{Float.parseFloat(sensorValuesString[0])};
                                break;
                            case 8:
                                streamDef = AndroidSenseConstants.PROXIMITY_STREAM_DEFINITION;
                                sensorName = AndroidSenseConstants.SENSOR_PROXIMITY;
                                payloadData = new Float[]{Float.parseFloat(sensorValuesString[0])};
                                break;
                            case 9:
                                streamDef = AndroidSenseConstants.GRAVITY_STREAM_DEFINITION;
                                sensorValues[0] = Float.parseFloat(sensorValuesString[0]) * Float.parseFloat(
                                        sensorValuesString[0]) * Float.
                                        parseFloat(sensorValuesString[0]);
                                payloadData = sensorValues;
                                sensorName = AndroidSenseConstants.SENSOR_GRAVITY;
                                break;
                            case 11:
                                streamDef = AndroidSenseConstants.ROTATION_STREAM_DEFINITION;
                                sensorValues[0] = Float.parseFloat(sensorValuesString[0]) * Float.parseFloat(
                                        sensorValuesString[0]) * Float.
                                        parseFloat(sensorValuesString[0]);
                                payloadData = sensorValues;
                                sensorName = AndroidSenseConstants.SENSOR_ROTATION;
                                break;
                        }
                    } catch (NumberFormatException e) {
                        log.error("Invalid sensor values are sent from the device.");
                        continue;
                    }
                }
                publishDataToDAS(deviceId, sensor.time, sensorName, streamDef, sensor.value, payloadData);
            }
        } catch (JsonSyntaxException e) {
            throw new TransportHandlerException("Invalid message format " + mqttMessage.toString());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void publishDataToDAS(String deviceId, Long time, String sensorName,
                                  String streamDefinition, String sensorValue, Object payloadData[]) {
        try {
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            DeviceManagementProviderService deviceManagementProviderService = (DeviceManagementProviderService) ctx
                    .getOSGiService(DeviceManagementProviderService.class, null);
            if (deviceManagementProviderService != null) {
                DeviceIdentifier identifier = new DeviceIdentifier(deviceId, AndroidSenseConstants.DEVICE_TYPE);
                Device device = deviceManagementProviderService.getDevice(identifier);
                if (device != null) {
                    String owner = device.getEnrolmentInfo().getOwner();
                    ctx.setTenantDomain(MultitenantUtils.getTenantDomain(owner), true);
                    DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx
                            .getOSGiService(DeviceAnalyticsService.class, null);
                    if (deviceAnalyticsService != null) {
                        Object metaData[] = {owner, AndroidSenseConstants.DEVICE_TYPE, deviceId, time};
                        if (streamDefinition != null && payloadData != null && payloadData.length > 0) {
                            try {
                                SensorDataManager.getInstance().setSensorRecord(deviceId, sensorName, sensorValue, time);
                                deviceAnalyticsService.publishEvent(streamDefinition, "1.0.0", metaData,
                                                                    new Object[0], payloadData);
                            } catch (DataPublisherConfigurationException e) {
                                log.error("Data publisher configuration failed - " + e);
                            }
                        }
                    }
                }
            }
        } catch (DeviceManagementException e) {
            log.error("Failed to load device management service.", e);
        }
    }

    /**
     * @throws TransportHandlerException in the event of any exceptions that occur whilst sending the message.
     */
    @Override
    public void publishDeviceData() throws TransportHandlerException {

    }

    /**
     * @param publishData the message (of the type specific to the protocol) to be sent to the device.
     * @throws TransportHandlerException in the event of any exceptions that occur whilst sending the message.
     */
    @Override
    public void publishDeviceData(MqttMessage publishData) throws TransportHandlerException {

    }

    @Override
    public void disconnect () {
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {
                    try {
                        closeConnection();
                    } catch (MqttException e) {
                        if (log.isDebugEnabled()) {
                            log.warn("Unable to 'STOP' MQTT connection at broker at: " + mqttBrokerEndPoint
                                         + " for device-type - " + AndroidSenseConstants.DEVICE_TYPE, e);
                        }
                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException e1) {
                            log.error("MQTT-Terminator: Thread Sleep Interrupt Exception at device-type - " +
                                          AndroidSenseConstants.DEVICE_TYPE, e1);
                        }
                    }
                }
            }
        };
        Thread terminatorThread = new Thread(stopConnection);
        terminatorThread.setDaemon(true);
        terminatorThread.start();
    }
}
