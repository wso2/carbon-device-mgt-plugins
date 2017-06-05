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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStatus;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentInfo;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentTransformer;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentValidator;
import org.wso2.carbon.device.mgt.input.adapter.mqtt.internal.InputAdapterServiceDataHolder;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MQTTAdapterListener implements MqttCallback, Runnable {
    private static final Log log = LogFactory.getLog(MQTTAdapterListener.class);

    private MqttClient mqttClient;
    private MqttConnectOptions connectionOptions;
    private boolean cleanSession;
    private boolean connectionInitialized;

    private MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration;
    private String topic;
    private String tenantDomain;
    private boolean connectionSucceeded = false;
    private ContentValidator contentValidator;
    private ContentTransformer contentTransformer;
    private InputEventAdapterConfiguration inputEventAdapterConfiguration;

    private InputEventAdapterListener eventAdapterListener = null;

    public MQTTAdapterListener(MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration,
                               String topic, InputEventAdapterConfiguration inputEventAdapterConfiguration,
                               InputEventAdapterListener inputEventAdapterListener) {
        String mqttClientId = inputEventAdapterConfiguration.getProperties()
                .get(MQTTEventAdapterConstants.ADAPTER_CONF_CLIENTID);
        if (mqttClientId == null || mqttClientId.trim().isEmpty()) {
            mqttClientId = MqttClient.generateClientId();
        }
        this.inputEventAdapterConfiguration = inputEventAdapterConfiguration;
        this.mqttBrokerConnectionConfiguration = mqttBrokerConnectionConfiguration;
        this.cleanSession = mqttBrokerConnectionConfiguration.isCleanSession();
        int keepAlive = mqttBrokerConnectionConfiguration.getKeepAlive();
        this.topic = PropertyUtils.replaceTenantDomainProperty(topic);
        this.eventAdapterListener = inputEventAdapterListener;
        this.tenantDomain = this.topic.split("/")[0];

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
            String contentValidatorType = this.mqttBrokerConnectionConfiguration.getContentValidatorType();

            if (contentValidatorType == null || contentValidatorType.equals(MQTTEventAdapterConstants.DEFAULT)) {
                contentValidator = InputAdapterServiceDataHolder.getInputAdapterExtensionService()
                        .getDefaultContentValidator();
            } else {
                contentValidator = InputAdapterServiceDataHolder.getInputAdapterExtensionService()
                        .getContentValidator(contentValidatorType);
            }

            String contentTransformerType = this.mqttBrokerConnectionConfiguration.getContentTransformerType();
            if (contentTransformerType == null || contentTransformerType.equals(MQTTEventAdapterConstants.DEFAULT)) {
                contentTransformer = InputAdapterServiceDataHolder.getInputAdapterExtensionService()
                        .getDefaultContentTransformer();
            } else {
                contentTransformer = InputAdapterServiceDataHolder.getInputAdapterExtensionService()
                        .getContentTransformer(contentTransformerType);
            }
        } catch (MqttException e) {
            log.error("Exception occurred while subscribing to MQTT broker at "
                    + mqttBrokerConnectionConfiguration.getBrokerUrl());
            throw new InputEventAdapterRuntimeException(e);
        }
    }

    public boolean startListener() throws MqttException {
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
                    if (!mqttBrokerConnectionConfiguration.isGlobalCredentailSet()) {
                        registrationProfile.setClientName(MQTTEventAdapterConstants.APPLICATION_NAME_PREFIX
                                                                  + mqttBrokerConnectionConfiguration.getAdapterName() +
                                                                  "_" + tenantDomain);
                        registrationProfile.setIsSaasApp(false);
                    } else {
                        registrationProfile.setClientName(MQTTEventAdapterConstants.APPLICATION_NAME_PREFIX
                                                                  + mqttBrokerConnectionConfiguration.getAdapterName());
                        registrationProfile.setIsSaasApp(true);
                    }
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
                } catch (HttpHostConnectException e) {
                    log.error("Keymanager is unreachable, Waiting....");
                    return false;
                } catch (MalformedURLException e) {
                    log.error("Invalid dcrUrl : " + dcrUrlString);
                    return false;
                } catch (JWTClientException | UserStoreException e) {
                    log.error("Failed to create an oauth token with jwt grant type.", e);
                    return false;
                } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | IOException e) {
                    log.error("Failed to create a http connection.", e);
                    return false;
                }
            }
        }
        try {
            mqttClient.connect(connectionOptions);
        } catch (MqttException e) {
            log.warn("Broker is unreachable, Waiting.....");
            return false;
        }
        try {
            mqttClient.subscribe(topic);
            log.info("mqtt receiver subscribed to topic: " + topic);
        } catch (MqttException e) {
            log.error("Failed to subscribe to topic: " + topic + ", Retrying.....");
            try {
                mqttClient.disconnect();
            } catch (MqttException ex) {
                // do nothing.
            }
            return false;
        }
        return true;

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
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            if (!tenantDomain.equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                TenantAxisUtils.getTenantConfigurationContext(tenantDomain, InputAdapterServiceDataHolder.getMainServerConfigContext());
            }

            InputEventAdapterListener inputEventAdapterListener = InputAdapterServiceDataHolder
                    .getInputEventAdapterService().getInputAdapterRuntime(PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getTenantId(), inputEventAdapterConfiguration.getName());

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
                    inputEventAdapterListener.onEvent(contentInfo.getMessage());
                }
            } else {
                inputEventAdapterListener.onEvent(msgText);
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
                if (connectionDuration > MQTTEventAdapterConstants.MAXIMUM_RECONNECTION_DURATION) {
                    connectionDuration = MQTTEventAdapterConstants.MAXIMUM_RECONNECTION_DURATION;
                }
                Thread.sleep(connectionDuration);
                if (startListener()) {
                    connectionSucceeded = true;
                    log.info("MQTT Connection successful");
                }
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
            throws UserStoreException, JWTClientException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        try {
            String scopes = mqttBrokerConnectionConfiguration.getBrokerScopes();
            String username = mqttBrokerConnectionConfiguration.getUsername();
            if (mqttBrokerConnectionConfiguration.isGlobalCredentailSet()) {
                username = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .getUserRealm().getRealmConfiguration().getAdminUserName() + "@" + PrivilegedCarbonContext
                        .getThreadLocalCarbonContext().getTenantDomain(true);
            }

            JWTClientManagerService jwtClientManagerService =
                    InputAdapterServiceDataHolder.getJwtClientManagerService();
            AccessTokenInfo accessTokenInfo = jwtClientManagerService.getJWTClient().getAccessToken(
                    clientId, clientSecret, username, scopes);
            return accessTokenInfo.getAccessToken();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private String getBase64Encode(String key, String value) {
        return new String(Base64.encodeBase64((key + ":" + value).getBytes()));
    }
}
