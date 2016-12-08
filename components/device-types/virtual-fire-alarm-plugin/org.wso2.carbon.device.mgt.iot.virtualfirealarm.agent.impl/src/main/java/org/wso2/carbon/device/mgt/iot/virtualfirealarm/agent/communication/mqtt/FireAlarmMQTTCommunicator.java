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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.communication.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.core.AgentConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.core.AgentManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.core.AgentUtilOperations;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.exception.AgentCoreOperationException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.transport.mqtt.MQTTTransportHandler;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

//TODO:: Lincence header, comments and SPECIFIC class name since its not generic
public class FireAlarmMQTTCommunicator extends MQTTTransportHandler {

    private static final Log log = LogFactory.getLog(FireAlarmMQTTCommunicator.class);

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> dataPushServiceHandler;
    private static final String DEFAULT_PASSWORD = "";

    public FireAlarmMQTTCommunicator(String deviceOwner, String deviceType,
                                     String mqttBrokerEndPoint, String subscribeTopic) {
        super(deviceOwner, deviceType, mqttBrokerEndPoint, subscribeTopic);
    }

    @SuppressWarnings("unused")
    public FireAlarmMQTTCommunicator(String deviceOwner, String deviceType,
                                     String mqttBrokerEndPoint, String subscribeTopic,
                                     int intervalInMillis) {
        super(deviceOwner, deviceType, mqttBrokerEndPoint, subscribeTopic, intervalInMillis);
    }

    public ScheduledFuture<?> getDataPushServiceHandler() {
        return dataPushServiceHandler;
    }

    //TODO:: Terminate logs with a period
    //TODO: Need to print exceptions
    @Override
    public void connect() {
        final AgentManager agentManager = AgentManager.getInstance();
        Runnable connector = new Runnable() {
            public void run() {
                while (!isConnected()) {
                    try {
                        connectToQueue(agentManager.getAgentConfigs().getAuthToken(), DEFAULT_PASSWORD);
                        agentManager.updateAgentStatus("Connected to MQTT Queue");
                    } catch (TransportHandlerException e) {
                        log.warn(AgentConstants.LOG_APPENDER + "Connection to MQTT Broker at: " + mqttBrokerEndPoint +
                                         " failed.\n Will retry in " + timeoutInterval + " milli-seconds.");

                        if (e.getCause() != null && e.getCause() instanceof MqttSecurityException) {
                            refreshOAuthToken((MqttSecurityException) e.getCause());
                        }
                    }

                    try {
                        if (isConnected()) {
                            subscribeToQueue();
                            agentManager.updateAgentStatus("Subscribed to MQTT Queue");
                            publishDeviceData();
                        }
                    } catch (TransportHandlerException e) {
                        log.warn(AgentConstants.LOG_APPENDER + "Subscription to MQTT Broker at: " +
                                         mqttBrokerEndPoint + " failed");
                        agentManager.updateAgentStatus("Subscription to broker failed.");
                    }

                    try {
                        Thread.sleep(timeoutInterval);
                    } catch (InterruptedException ex) {
                        log.error(AgentConstants.LOG_APPENDER + "MQTT: Connect-Thread Sleep Interrupt Exception.");
                    }
                }
            }
        };

        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

    private void refreshOAuthToken(final MqttSecurityException exception) {
        Runnable tokenRefresher = new Runnable() {
            public void run() {
                String authenticationMethod = AgentUtilOperations.getAuthenticationMethod();

                try {
                    if (exception.getReasonCode() == MqttSecurityException.REASON_CODE_FAILED_AUTHENTICATION &&
                            authenticationMethod.equals(AgentConstants.TOKEN_AUTHENTICATION_METHOD)) {
                        AgentUtilOperations.refreshOAuthToken();
                    }
                } catch (AgentCoreOperationException e1) {
                    log.error(AgentConstants.LOG_APPENDER + "Token Refresh Attempt Failed. " + e1);
                }
            }
        };

        Thread connectorThread = new Thread(tokenRefresher);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

    @Override
    public void processIncomingMessage(MqttMessage message, String... messageParams) {
        final AgentManager agentManager = AgentManager.getInstance();
        String tenantDomain = agentManager.getAgentConfigs().getTenantDomain();
        String deviceOwner = agentManager.getAgentConfigs().getDeviceOwner();
        String deviceID = agentManager.getAgentConfigs().getDeviceId();
        String receivedMessage;
        String replyMessage;
        String securePayLoad;

        try {
            receivedMessage = AgentUtilOperations.extractMessageFromPayload(message.toString());
            log.info(AgentConstants.LOG_APPENDER + "Message [" + receivedMessage + "] was received");
        } catch (AgentCoreOperationException e) {
            log.warn(AgentConstants.LOG_APPENDER + "Could not extract message from payload.", e);
            return;
        }


        String[] controlSignal = receivedMessage.split(":");
        // message- "<SIGNAL_TYPE>:<SIGNAL_MODE>" format.(ex: "BULB:ON", "TEMPERATURE", "HUMIDITY")

        try {
            switch (controlSignal[0].toUpperCase()) {
                case AgentConstants.BULB_CONTROL:
                    boolean stateToSwitch = controlSignal[1].equals(AgentConstants.CONTROL_ON);
                    agentManager.changeAlarmStatus(stateToSwitch);
                    log.info(AgentConstants.LOG_APPENDER + "Bulb was switched to state: '" + controlSignal[1] + "'");
                    break;

                case AgentConstants.TEMPERATURE_CONTROL:
                    int currentTemperature = agentManager.getTemperature();

                    String replyTemperature = "Current temperature was read as: '" + currentTemperature + "C'";
                    log.info(AgentConstants.LOG_APPENDER + replyTemperature);

                    String tempPublishTopic = String.format(AgentConstants.MQTT_PUBLISH_TOPIC, tenantDomain, deviceID);

                    replyMessage = AgentConstants.TEMPERATURE_CONTROL + ":" + currentTemperature;
                    securePayLoad = AgentUtilOperations.prepareSecurePayLoad(replyMessage);
                    publishToQueue(tempPublishTopic, securePayLoad);
                    break;

                case AgentConstants.HUMIDITY_CONTROL:
                    int currentHumidity = agentManager.getHumidity();

                    String replyHumidity = "Current humidity was read as: '" + currentHumidity + "%'";
                    log.info(AgentConstants.LOG_APPENDER + replyHumidity);

                    String humidPublishTopic = String.format(
                            AgentConstants.MQTT_PUBLISH_TOPIC, tenantDomain, deviceID);

                    replyMessage = AgentConstants.HUMIDITY_CONTROL + ":" + currentHumidity;
                    securePayLoad = AgentUtilOperations.prepareSecurePayLoad(replyMessage);
                    publishToQueue(humidPublishTopic, securePayLoad);
                    break;

                default:
                    log.warn(AgentConstants.LOG_APPENDER + "'" + controlSignal[0] +
                                     "' is invalid and not-supported for this device-type");
                    break;
            }
        } catch (AgentCoreOperationException e) {
            log.warn(AgentConstants.LOG_APPENDER + "Preparing Secure payload failed", e);
        } catch (TransportHandlerException e) {
            log.error(AgentConstants.LOG_APPENDER +
                              "MQTT - Publishing, reply message to the MQTT Queue  at: " +
                              agentManager.getAgentConfigs().getMqttBrokerEndpoint() + " failed");
        }

    }

    @Override
    public void publishDeviceData() {
        final AgentManager agentManager = AgentManager.getInstance();
        int publishInterval = agentManager.getPushInterval();
        Runnable pushDataRunnable = new Runnable() {
            @Override
            public void run() {
                int currentTemperature = agentManager.getTemperature();
                String message = "PUBLISHER:" + AgentConstants.TEMPERATURE_CONTROL + ":" + currentTemperature;

                try {
                    String payLoad = AgentUtilOperations.prepareSecurePayLoad(message);

                    MqttMessage pushMessage = new MqttMessage();
                    pushMessage.setPayload(payLoad.getBytes(StandardCharsets.UTF_8));
                    pushMessage.setQos(DEFAULT_MQTT_QUALITY_OF_SERVICE);
                    pushMessage.setRetained(false);

                    String topic = String.format(AgentConstants.MQTT_PUBLISH_TOPIC,
                                                 agentManager.getAgentConfigs().getTenantDomain(),
                                                 agentManager.getAgentConfigs().getDeviceId());

                    publishToQueue(topic, pushMessage);
                    log.info(AgentConstants.LOG_APPENDER + "Message: '" + message + "' published to MQTT Queue at [" +
                                     agentManager.getAgentConfigs().getMqttBrokerEndpoint() + "] under topic [" +
                                     topic + "]");

                } catch (TransportHandlerException e) {
                    log.warn(AgentConstants.LOG_APPENDER + "Data Publish attempt to topic - [" +
                                     AgentConstants.MQTT_PUBLISH_TOPIC + "] failed for payload [" + message + "]");
                } catch (AgentCoreOperationException e) {
                    log.warn(AgentConstants.LOG_APPENDER + "Preparing Secure payload failed", e);
                }
            }
        };

        dataPushServiceHandler = service.scheduleAtFixedRate(pushDataRunnable, publishInterval, publishInterval,
                                                             TimeUnit.SECONDS);
    }


    @Override
    public void disconnect() {
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {

                    if (dataPushServiceHandler != null) {
                        dataPushServiceHandler.cancel(true);
                    }

                    try {
                        closeConnection();

                    } catch (MqttException e) {
                        if (log.isDebugEnabled()) {
                            log.warn(AgentConstants.LOG_APPENDER +
                                             "Unable to 'STOP' MQTT connection at broker at: " +
                                             mqttBrokerEndPoint);
                        }

                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException e1) {
                            log.error(AgentConstants.LOG_APPENDER +
                                              "MQTT-Terminator: Thread Sleep Interrupt Exception");
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
    public void processIncomingMessage() {

    }

    @Override
    public void publishDeviceData(String... publishData) {

    }

}
