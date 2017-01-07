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
package org.wso2.carbon.device.mgt.output.adapter.mqtt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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
            if (mqttBrokerConnectionConfiguration.getUsername() != null) {
                connectionOptions.setUserName(getToken());
                connectionOptions.setPassword(MQTTEventAdapterConstants.DEFAULT_PASSWORD.toCharArray());
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

    private String getToken() {
        String username = this.mqttBrokerConnectionConfiguration.getUsername();
        String password = this.mqttBrokerConnectionConfiguration.getPassword();
        String dcrUrlString = this.mqttBrokerConnectionConfiguration.getDcrUrl();

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (dcrUrlString != null && !dcrUrlString.isEmpty()) {
            try {
                URL dcrUrl = new URL(dcrUrlString);
                HttpClient httpClient = MQTTUtil.getHttpClient(dcrUrl.getProtocol());
                HttpPost postMethod = new HttpPost(dcrUrlString);
                RegistrationProfile registrationProfile = new RegistrationProfile();
                registrationProfile.setCallbackUrl(MQTTEventAdapterConstants.EMPTY_STRING);
                registrationProfile.setGrantType(MQTTEventAdapterConstants.GRANT_TYPE);
                registrationProfile.setOwner(username);
                registrationProfile.setTokenScope(MQTTEventAdapterConstants.TOKEN_SCOPE);
                registrationProfile.setClientName(MQTTEventAdapterConstants.APPLICATION_NAME_PREFIX
                                + mqttBrokerConnectionConfiguration.getAdapterName() + "_" + tenantId);
                String jsonString = registrationProfile.toJSON();
                StringEntity requestEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
                postMethod.setEntity(requestEntity);
                String basicAuth = getBase64Encode(username, password);
                postMethod.setHeader(new BasicHeader(MQTTEventAdapterConstants.AUTHORIZATION_HEADER_NAME,
                                                     MQTTEventAdapterConstants.AUTHORIZATION_HEADER_VALUE_PREFIX +
                                                             basicAuth));
                HttpResponse httpResponse = httpClient.execute(postMethod);
                if (httpResponse != null) {
                    String response = MQTTUtil.getResponseString(httpResponse);
                    try {
                        if (response != null) {
                            JSONParser jsonParser = new JSONParser();
                            JSONObject jsonPayload = (JSONObject) jsonParser.parse(response);
                            String clientId = (String) jsonPayload.get(MQTTEventAdapterConstants.CLIENT_ID);
                            String clientSecret = (String) jsonPayload.get(MQTTEventAdapterConstants.CLIENT_SECRET);
                            getToken(clientId, clientSecret);
                        }
                    } catch (ParseException e) {
                        String msg = "error occurred while parsing generating token for the adapter";
                        log.error(msg, e);
                    }
                }
            } catch (MalformedURLException e) {
                throw new OutputEventAdapterRuntimeException("Invalid dcrUrl : " + dcrUrlString);
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
                throw new OutputEventAdapterRuntimeException("Failed to create an https connection.", e);
            }
        }
        throw new OutputEventAdapterRuntimeException("Invalid configuration for mqtt publisher");
    }

    private String getToken(String clientId, String clientSecret)
            throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        URL tokenEndpoint = new URL(mqttBrokerConnectionConfiguration.getTokenUrl());
        HttpClient httpClient = MQTTUtil.getHttpClient(tokenEndpoint.getProtocol());
        HttpPost postMethod = new HttpPost(tokenEndpoint.toString());

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair(MQTTEventAdapterConstants.GRANT_TYPE_PARAM_NAME,
                                                  MQTTEventAdapterConstants.PASSWORD_GRANT_TYPE));
        nameValuePairs.add(new BasicNameValuePair(MQTTEventAdapterConstants.PASSWORD_GRANT_TYPE_USERNAME,
                                                  mqttBrokerConnectionConfiguration.getUsername()));
        nameValuePairs.add(new BasicNameValuePair(MQTTEventAdapterConstants.PASSWORD_GRANT_TYPE_PASSWORD,
                                                  mqttBrokerConnectionConfiguration.getPassword()));
        String scopes = mqttBrokerConnectionConfiguration.getScopes();
        if (scopes != null && !scopes.isEmpty()) {
            nameValuePairs.add(new BasicNameValuePair(MQTTEventAdapterConstants.PASSWORD_GRANT_TYPE_SCOPES, scopes));
        }

        postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        postMethod.addHeader("Authorization", "Basic " + getBase64Encode(clientId, clientSecret));
        postMethod.addHeader("Content-Type", "application/x-www-form-urlencoded");
        HttpResponse httpResponse = httpClient.execute(postMethod);
        String response = MQTTUtil.getResponseString(httpResponse);
        if (log.isDebugEnabled()) {
            log.debug(response);
        }
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
        return (String) jsonObject.get(MQTTEventAdapterConstants.ACCESS_TOKEN_GRANT_TYPE_PARAM_NAME);
    }

    private String getBase64Encode(String key, String value) {
        return new String(Base64.encodeBase64((key + ":" + value).getBytes()));
    }

}
