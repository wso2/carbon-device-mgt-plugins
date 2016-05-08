/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.device.mgt.iot.output.adapter.xmpp.i18n.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * MQTT publisher related configuration initialization and publishing capabilties are implemented here.
 */
public class MQTTAdapterPublisher {

    private static final Log log = LogFactory.getLog(MQTTAdapterPublisher.class);
    private MqttClient mqttClient;
    private MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration;
    String clientId;

    public MQTTAdapterPublisher(MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration, String clientId) {
        if (clientId == null || clientId.trim().isEmpty()) {
            this.clientId = MqttClient.generateClientId();
        }
        this.mqttBrokerConnectionConfiguration = mqttBrokerConnectionConfiguration;
        connect();
    }

    public void connect() {
        if (clientId == null || clientId.trim().isEmpty()) {
            clientId = MqttClient.generateClientId();
        }
        boolean cleanSession = mqttBrokerConnectionConfiguration.isCleanSession();
        int keepAlive = mqttBrokerConnectionConfiguration.getKeepAlive();
        String temp_directory = System.getProperty(MQTTEventAdapterConstants.ADAPTER_TEMP_DIRECTORY_NAME);
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(temp_directory);
        try {
            MqttConnectOptions connectionOptions = new MqttConnectOptions();
            connectionOptions.setCleanSession(cleanSession);
            connectionOptions.setKeepAliveInterval(keepAlive);
            if (mqttBrokerConnectionConfiguration.getBrokerUsername() != null) {
                connectionOptions.setUserName(getToken(mqttBrokerConnectionConfiguration.getBrokerUsername(),
                                                       mqttBrokerConnectionConfiguration.getDcrUrl(),
                                                       mqttBrokerConnectionConfiguration.getScopes()));
                connectionOptions.setPassword(Constants.DEFAULT_PASSWORD.toCharArray());
            }
            // Construct an MQTT blocking mode client
            mqttClient = new MqttClient(mqttBrokerConnectionConfiguration.getBrokerUrl(), clientId, dataStore);
            mqttClient.connect(connectionOptions);

        } catch (MqttException e) {
            log.error("Error occurred when constructing MQTT client for broker url : "
                              + mqttBrokerConnectionConfiguration.getBrokerUrl(), e);
            handleException(e);
        }
    }

    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    public void publish(int qos, String payload, String topic) {
        try {
            // Create and configure a message
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            log.error("Error occurred when publishing message for MQTT server : " + mqttClient.getServerURI(), e);
            handleException(e);
        }
    }

    public void publish(String payload, String topic) {
        try {
            // Create and configure a message
            MqttMessage message = new MqttMessage(payload.getBytes());
            mqttClient.publish(topic, message);
        } catch (MqttException e) {
            log.error("Error occurred when publishing message for MQTT server : " + mqttClient.getServerURI(), e);
            handleException(e);
        }
    }

    public void close() throws OutputEventAdapterException {
        try {
            mqttClient.disconnect(1000);
            mqttClient.close();
        } catch (MqttException e) {
            throw new OutputEventAdapterException(e);
        }
    }

    private void handleException(MqttException e) {
        //Check for Client not connected exception code and throw ConnectionUnavailableException
        if (e.getReasonCode() == 32104) {
            throw new ConnectionUnavailableException(e);
        } else {
            throw new OutputEventAdapterRuntimeException(e);
        }
    }

    private String getToken(String username, String dcrUrlString, String scopes) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (dcrUrlString != null && !dcrUrlString.isEmpty()) {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
            try {
                URL dcrUrl = new URL(dcrUrlString);
                HttpClient httpClient = MQTTUtil.getHttpClient(dcrUrl.getProtocol());
                HttpPost postMethod = new HttpPost(dcrUrlString);
                RegistrationProfile registrationProfile = new RegistrationProfile();
                registrationProfile.setCallbackUrl(Constants.DEFAULT_CALLBACK);
                registrationProfile.setGrantType(Constants.GRANT_TYPE);
                registrationProfile.setOwner(username);
                registrationProfile.setTokenScope(Constants.TOKEN_SCOPE);
                registrationProfile.setApplicationType(Constants.APPLICATION_TYPE);
                registrationProfile.setClientName(username + "_" + tenantId);
                String jsonString = registrationProfile.toJSON();
                StringEntity requestEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
                postMethod.setEntity(requestEntity);
                HttpResponse httpResponse = httpClient.execute(postMethod);
                String response = MQTTUtil.getResponseString(httpResponse);
                try {
                    JSONParser jsonParser = new JSONParser();
                    JSONObject jsonPayload = (JSONObject) jsonParser.parse(response);
                    String clientId = (String) jsonPayload.get(Constants.CLIENT_ID);
                    String clientSecret = (String) jsonPayload.get(Constants.CLIENT_SECRET);
                    JWTClientManagerService jwtClientManagerService = MQTTUtil.getJWTClientManagerService();
                    AccessTokenInfo accessTokenInfo = jwtClientManagerService.getJWTClient().getAccessToken(
                            clientId, clientSecret, username, scopes);
                    return accessTokenInfo.getAccessToken();
                } catch (ParseException e) {
                    String msg = "error occurred while parsing client credential payload";
                    throw new OutputEventAdapterRuntimeException(msg, e);
                } catch (JWTClientException e) {
                    String msg = "error occurred while parsing the response from JWT Client";
                    throw new OutputEventAdapterRuntimeException(msg, e);
                }
            } catch (MalformedURLException e) {
                throw new OutputEventAdapterRuntimeException("Invalid dcrUrl : " + dcrUrlString);
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
                throw new OutputEventAdapterRuntimeException("Failed to create an https connection.", e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        throw new OutputEventAdapterRuntimeException("Invalid configuration for mqtt publisher");
    }

}
