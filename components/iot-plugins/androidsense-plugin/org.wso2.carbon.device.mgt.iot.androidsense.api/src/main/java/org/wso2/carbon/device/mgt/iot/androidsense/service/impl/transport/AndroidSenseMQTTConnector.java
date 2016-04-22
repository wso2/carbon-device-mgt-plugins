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

package org.wso2.carbon.device.mgt.iot.androidsense.service.impl.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.androidsense.plugin.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.transport.mqtt.MQTTTransportHandler;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SuppressWarnings("no JAX-WS annotation")
public class AndroidSenseMQTTConnector extends MQTTTransportHandler {
    private static Log log = LogFactory.getLog(AndroidSenseMQTTConnector.class);
    private static String subscribeTopic = AndroidSenseConstants.MQTT_SUBSCRIBE_WORDS_TOPIC;
    private static String iotServerSubscriber = UUID.randomUUID().toString().substring(0, 5);
    private static final String KEY_TYPE = "PRODUCTION";
    private static final String DEFAULT_PASSWORD = "";

    private AndroidSenseMQTTConnector() {
        super(iotServerSubscriber, AndroidSenseConstants.DEVICE_TYPE,
              MqttConfig.getInstance().getMqttQueueEndpoint(), subscribeTopic);
    }

    @Override
    public void connect() {
        Runnable connector = new Runnable() {
            public void run() {
                while (!isConnected()) {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                            AndroidSenseConstants.DEVICE_TYPE_PROVIDER_DOMAIN, true);
                    try {
                        String applicationUsername = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                                .getRealmConfiguration().getAdminUserName();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(applicationUsername);
                        APIManagementProviderService apiManagementProviderService = APIUtil
                                .getAPIManagementProviderService();
                        String[] tags = {AndroidSenseConstants.DEVICE_TYPE};
                        ApiApplicationKey apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                                AndroidSenseConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true);
                        JWTClient jwtClient = APIUtil.getJWTClientManagerService().getJWTClient();
                        String scopes = "device_type_" + AndroidSenseConstants.DEVICE_TYPE + " device_mqtt_connector";
                        AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(),
                            apiApplicationKey.getConsumerSecret(), applicationUsername, scopes);
                        //create token
                        String accessToken = accessTokenInfo.getAccessToken();
                        setUsernameAndPassword(accessToken, DEFAULT_PASSWORD);
                        connectToQueue();
                    } catch (TransportHandlerException e) {
                        log.warn("Connection/Subscription to MQTT Broker at: " + mqttBrokerEndPoint + " failed");
                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException ex) {
                            log.error("MQTT-Subscriber: Thread Sleep Interrupt Exception.", ex);
                        }
                    }catch (JWTClientException e) {
                        log.error("Failed to retrieve token from JWT Client.", e);
                        return;
                    } catch (UserStoreException e) {
                        log.error("Failed to retrieve the user.", e);
                        return;
                    } catch (APIManagerException e) {
                        log.error("Failed to create an application and generate keys.", e);
                        return;
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
        };
        Thread connectorThread = new Thread(connector);
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
        terminatorThread.start();
    }
}
