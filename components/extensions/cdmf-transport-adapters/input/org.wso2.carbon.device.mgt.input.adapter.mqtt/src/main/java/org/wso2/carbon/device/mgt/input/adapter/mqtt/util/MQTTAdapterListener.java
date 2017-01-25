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
package org.wso2.carbon.device.mgt.input.adapter.mqtt.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStatus;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentInfo;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentTransformer;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentValidator;
import org.wso2.carbon.device.mgt.input.adapter.extension.DefaultContentTransformer;
import org.wso2.carbon.device.mgt.input.adapter.extension.DefaultContentValidator;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.device.mgt.input.adapter.mqtt.exception.MQTTContentInitializationException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MQTTAdapterListener implements MqttCallback, Runnable {
    private static final Log log = LogFactory.getLog(MQTTAdapterListener.class);

    private MqttClient mqttClient;
    private MqttConnectOptions connectionOptions;
    private boolean cleanSession;
    private boolean connectionInitialized;

    private MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration;
    private String topic;
    private int tenantId;
    private boolean connectionSucceeded = false;
    ContentValidator contentValidator;
    ContentTransformer contentTransformer;

    private InputEventAdapterListener eventAdapterListener = null;

    public MQTTAdapterListener(MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration,
                               String topic, String mqttClientId,
                               InputEventAdapterListener inputEventAdapterListener, int tenantId) {

        if(mqttClientId == null || mqttClientId.trim().isEmpty()){
            mqttClientId = MqttClient.generateClientId();
        }
        this.mqttBrokerConnectionConfiguration = mqttBrokerConnectionConfiguration;
        this.cleanSession = mqttBrokerConnectionConfiguration.isCleanSession();
        int keepAlive = mqttBrokerConnectionConfiguration.getKeepAlive();
        this.topic = topic;
        this.eventAdapterListener = inputEventAdapterListener;
        this.tenantId = tenantId;

        //SORTING messages until the server fetches them
        String temp_directory = System.getProperty("java.io.tmpdir");
        MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(temp_directory);

        try {
            connectionOptions = new MqttConnectOptions();
            connectionOptions.setCleanSession(cleanSession);
            connectionOptions.setKeepAliveInterval(keepAlive);

            // Construct an MQTT blocking mode client
            mqttClient = new MqttClient(this.mqttBrokerConnectionConfiguration.getBrokerUrl(), mqttClientId,
                    dataStore);

            // Set this wrapper as the callback handler
            mqttClient.setCallback(this);
            String contentValidatorClassName = this.mqttBrokerConnectionConfiguration.getContentValidatorClassName();

            if (contentValidatorClassName != null && contentValidatorClassName.equals(MQTTEventAdapterConstants.DEFAULT)) {
                    contentValidator = new DefaultContentValidator();
            } else if (contentValidatorClassName != null && !contentValidatorClassName.isEmpty()) {
                try {
                    Class<? extends ContentValidator> contentValidatorClass = Class.forName(contentValidatorClassName)
                            .asSubclass(ContentValidator.class);
                    contentValidator = contentValidatorClass.newInstance();
                } catch (ClassNotFoundException e) {
                    throw new MQTTContentInitializationException(
                            "Unable to find the class validator: " + contentValidatorClassName, e);
                } catch (InstantiationException e) {
                    throw new MQTTContentInitializationException(
                            "Unable to create an instance of :" + contentValidatorClassName, e);
                } catch (IllegalAccessException e) {
                    throw new MQTTContentInitializationException("Access of the instance in not allowed.", e);
                }
            }

            String contentTransformerClassName = this.mqttBrokerConnectionConfiguration.getContentTransformerClassName();
            if (contentTransformerClassName != null && contentTransformerClassName.equals(MQTTEventAdapterConstants.DEFAULT)) {
                contentTransformer = new DefaultContentTransformer();
            } else if (contentTransformerClassName != null && !contentTransformerClassName.isEmpty()) {
                try {
                    Class<? extends ContentTransformer> contentTransformerClass = Class.forName(contentTransformerClassName)
                            .asSubclass(ContentTransformer.class);
                    contentTransformer = contentTransformerClass.newInstance();
                } catch (ClassNotFoundException e) {
                    throw new MQTTContentInitializationException(
                            "Unable to find the class transfoer: " + contentTransformerClassName, e);
                } catch (InstantiationException e) {
                    throw new MQTTContentInitializationException(
                            "Unable to create an instance of :" + contentTransformerClassName, e);
                } catch (IllegalAccessException e) {
                    throw new MQTTContentInitializationException("Access of the instance in not allowed.", e);
                }
            }
        } catch (MqttException e) {
            log.error("Exception occurred while subscribing to MQTT broker at "
                    + mqttBrokerConnectionConfiguration.getBrokerUrl());
            throw new InputEventAdapterRuntimeException(e);
        }
    }

    public void startListener() throws MqttException {
        if (this.mqttBrokerConnectionConfiguration.getUsername() != null &&
                this.mqttBrokerConnectionConfiguration.getDcrUrl() != null) {
            String username = this.mqttBrokerConnectionConfiguration.getUsername();
            String password = this.mqttBrokerConnectionConfiguration.getPassword();
            String dcrUrlString = this.mqttBrokerConnectionConfiguration.getDcrUrl();
            String scopes = this.mqttBrokerConnectionConfiguration.getBrokerScopes();
            //getJWT Client Parameters.
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
                                connectionOptions.setUserName(getToken(clientId, clientSecret));
                            }
                        } catch (ParseException e) {
                            String msg = "error occurred while parsing generating token for the adapter";
                            log.error(msg, e);
                        }
                    }
                } catch (MalformedURLException e) {
                    log.error("Invalid dcrUrl : " + dcrUrlString);
                } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException  e) {
                    log.error("Failed to create an https connection.", e);
                }
            }
        }
        mqttClient.connect(connectionOptions);
        mqttClient.subscribe(topic);
    }

    public void stopListener(String adapterName) {
        if (connectionSucceeded) {
            try {
                if (!ServerStatus.getCurrentStatus().equals(ServerStatus.STATUS_SHUTTING_DOWN) || cleanSession) {
                    mqttClient.unsubscribe(topic);
                }
                mqttClient.disconnect(3000);
            } catch (MqttException e) {
                log.error("Can not unsubscribe from the destination " + topic +
                                  " with the event adapter " + adapterName, e);
            }
        }
        connectionSucceeded = true;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        log.warn("MQTT connection not reachable " + throwable);
        connectionSucceeded = false;
        new Thread(this).start();
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        try {
            String msgText = mqttMessage.toString();
            if (log.isDebugEnabled()) {
                log.debug(msgText);
            }
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

            if (log.isDebugEnabled()) {
                log.debug("Event received in MQTT Event Adapter - " + msgText);
            }

            if (contentValidator != null && contentTransformer != null) {
                ContentInfo contentInfo;
                Map<String, Object> dynamicProperties = new HashMap<>();
                dynamicProperties.put(MQTTEventAdapterConstants.TOPIC, topic);
                msgText = (String) contentTransformer.transform(msgText, dynamicProperties);
                contentInfo = contentValidator.validate(msgText, dynamicProperties);
                if (contentInfo != null && contentInfo.isValidContent()) {
                    eventAdapterListener.onEvent(contentInfo.getMessage());
                }
            } else {
                eventAdapterListener.onEvent(msgText);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    @Override
    public void run() {
        int connectionDuration = MQTTEventAdapterConstants.INITIAL_RECONNECTION_DURATION;
        while (!connectionSucceeded) {
            try {
                connectionDuration = connectionDuration * MQTTEventAdapterConstants.RECONNECTION_PROGRESS_FACTOR;
                Thread.sleep(connectionDuration);
                startListener();
                connectionSucceeded = true;
                log.info("MQTT Connection successful");
            } catch (InterruptedException e) {
                log.error("Interruption occurred while waiting for reconnection", e);
            } catch (MqttException e) {
                log.error("MQTT Exception occurred when starting listener", e);
            }
        }
    }

    public void createConnection() {
        connectionInitialized = true;
        new Thread(this).start();
    }

    public boolean isConnectionInitialized() {
        return connectionInitialized;
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
        String scopes = mqttBrokerConnectionConfiguration.getBrokerScopes();
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
