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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.core;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.enrollment.EnrollmentManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.exception.AgentCoreOperationException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.transport.CommunicationUtils;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.transport.TransportUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

/**
 * This class contains all the core operations of the FireAlarm agent that are common to both
 * Virtual and Real Scenarios. These operations include, connecting to and subscribing to an MQTT
 * queue and to a XMPP Server. Pushing temperature data to the IoT-Server at timely intervals.
 * Reading device specific configuration from a configs file etc....
 */
public class AgentUtilOperations {

    private static final Log log = LogFactory.getLog(AgentUtilOperations.class);
    private static final String JSON_MESSAGE_KEY = "Msg";
    private static final String JSON_SIGNATURE_KEY = "Sig";
    private static final String JSON_SERIAL_KEY = "SerialNumber";

    /**
     * This method reads the agent specific configurations for the device from the
     * "deviceConfigs.properties" file found at /repository/conf folder.
     * If the properties file is not found in the specified path, then the configuration values
     * are set to the default ones in the 'AgentConstants' class.
     *
     * @return an object of type 'AgentConfiguration' which contains all the necessary
     * configuration attributes
     */
    public static AgentConfiguration readIoTServerConfigs() throws AgentCoreOperationException {
        AgentManager agentManager = AgentManager.getInstance();
        AgentConfiguration iotServerConfigs = new AgentConfiguration();
        Properties properties = new Properties();
        InputStream propertiesInputStream = null;
        String propertiesFileName = AgentConstants.AGENT_PROPERTIES_FILE_NAME;
        String rootPath = "";

        try {
            ClassLoader loader = AgentUtilOperations.class.getClassLoader();
            URL path = loader.getResource(propertiesFileName);

            if (path != null) {
                log.info(AgentConstants.LOG_APPENDER + path);
                rootPath = path.getPath().replace("wso2-firealarm-virtual-agent.jar!/deviceConfig.properties", "")
                        .replace("jar:", "").replace("file:", "");

                rootPath = URLDecoder.decode(rootPath, StandardCharsets.UTF_8.toString());
                agentManager.setRootPath(rootPath);

                String deviceConfigFilePath = rootPath + AgentConstants.AGENT_PROPERTIES_FILE_NAME;
                propertiesInputStream = new FileInputStream(deviceConfigFilePath);

                //load a properties file from class path, inside static method
                properties.load(propertiesInputStream);

                iotServerConfigs.setTenantDomain(properties.getProperty(
                        AgentConstants.TENANT_DOMAIN));
                iotServerConfigs.setDeviceOwner(properties.getProperty(
                        AgentConstants.DEVICE_OWNER_PROPERTY));
                iotServerConfigs.setDeviceId(properties.getProperty(
                        AgentConstants.DEVICE_ID_PROPERTY));
                iotServerConfigs.setDeviceName(properties.getProperty(
                        AgentConstants.DEVICE_NAME_PROPERTY));
                iotServerConfigs.setControllerContext(properties.getProperty(
                        AgentConstants.DEVICE_CONTROLLER_CONTEXT_PROPERTY));
                iotServerConfigs.setScepContext(properties.getProperty(
                        AgentConstants.DEVICE_SCEP_CONTEXT_PROPERTY));
                iotServerConfigs.setHTTPS_ServerEndpoint(properties.getProperty(
                        AgentConstants.SERVER_HTTPS_EP_PROPERTY));
                iotServerConfigs.setHTTP_ServerEndpoint(properties.getProperty(
                        AgentConstants.SERVER_HTTP_EP_PROPERTY));
                iotServerConfigs.setApimGatewayEndpoint(properties.getProperty(
                        AgentConstants.APIM_GATEWAY_EP_PROPERTY));
                iotServerConfigs.setMqttBrokerEndpoint(properties.getProperty(
                        AgentConstants.MQTT_BROKER_EP_PROPERTY));
                iotServerConfigs.setXmppServerEndpoint(properties.getProperty(
                        AgentConstants.XMPP_SERVER_EP_PROPERTY));
                iotServerConfigs.setXmppServerName(properties.getProperty(
                        AgentConstants.XMPP_SERVER_NAME_PROPERTY));
                iotServerConfigs.setApiApplicationKey(properties.getProperty(
                        AgentConstants.API_APPLICATION_KEY));
                iotServerConfigs.setAuthMethod(properties.getProperty(
                        AgentConstants.AUTH_METHOD_PROPERTY));
                iotServerConfigs.setAuthToken(properties.getProperty(
                        AgentConstants.AUTH_TOKEN_PROPERTY));
                iotServerConfigs.setRefreshToken(properties.getProperty(
                        AgentConstants.REFRESH_TOKEN_PROPERTY));
                iotServerConfigs.setDataPushInterval(Integer.parseInt(properties.getProperty(
                        AgentConstants.PUSH_INTERVAL_PROPERTY)));

                log.info(AgentConstants.LOG_APPENDER + "Tenant Domain: " +
                                 iotServerConfigs.getTenantDomain());
                log.info(AgentConstants.LOG_APPENDER + "Device Owner: " +
                                 iotServerConfigs.getDeviceOwner());
                log.info(AgentConstants.LOG_APPENDER + "Device ID: " + iotServerConfigs.getDeviceId());
                log.info(AgentConstants.LOG_APPENDER + "Device Name: " +
                                 iotServerConfigs.getDeviceName());
                log.info(AgentConstants.LOG_APPENDER + "Device Controller Context: " +
                                 iotServerConfigs.getControllerContext());
                log.info(AgentConstants.LOG_APPENDER + "IoT Server HTTPS EndPoint: " +
                                 iotServerConfigs.getHTTPS_ServerEndpoint());
                log.info(AgentConstants.LOG_APPENDER + "IoT Server HTTP EndPoint: " +
                                 iotServerConfigs.getHTTP_ServerEndpoint());
                log.info(AgentConstants.LOG_APPENDER + "API-Manager Gateway EndPoint: " +
                                 iotServerConfigs.getApimGatewayEndpoint());
                log.info(AgentConstants.LOG_APPENDER + "MQTT Broker EndPoint: " +
                                 iotServerConfigs.getMqttBrokerEndpoint());
                log.info(AgentConstants.LOG_APPENDER + "XMPP Server EndPoint: " +
                                 iotServerConfigs.getXmppServerEndpoint());
                log.info(AgentConstants.LOG_APPENDER + "Authentication Method: " +
                                 iotServerConfigs.getAuthMethod());
                log.info(AgentConstants.LOG_APPENDER + "Base64Encoded API Application Key: " +
                                 iotServerConfigs.getApiApplicationKey());
                log.info(AgentConstants.LOG_APPENDER + "Authentication Token: " +
                                 iotServerConfigs.getAuthToken());
                log.info(AgentConstants.LOG_APPENDER + "Refresh Token: " +
                                 iotServerConfigs.getRefreshToken());
                log.info(AgentConstants.LOG_APPENDER + "Data Push Interval: " +
                                 iotServerConfigs.getDataPushInterval());
                log.info(AgentConstants.LOG_APPENDER + "XMPP Server Name: " +
                                 iotServerConfigs.getXmppServerName());
            } else {
                throw new AgentCoreOperationException(
                        "Failed to load path of resource [" + propertiesFileName + "] from this classpath.");
            }
        } catch (FileNotFoundException ex) {
            String errorMsg = "[" + propertiesFileName + "] file not found at: " + rootPath;
            log.error(AgentConstants.LOG_APPENDER + errorMsg);
            throw new AgentCoreOperationException(errorMsg);

        } catch (IOException ex) {
            String errorMsg = "Error occurred whilst trying to fetch [" + propertiesFileName + "] from: " +
                    AgentConstants.PROPERTIES_FILE_PATH;
            log.error(AgentConstants.LOG_APPENDER + errorMsg);
            throw new AgentCoreOperationException(errorMsg);
        } finally {
            if (propertiesInputStream != null) {
                try {
                    propertiesInputStream.close();
                } catch (IOException e) {
                    log.error(AgentConstants.LOG_APPENDER +
                                      "Error occurred whilst trying to close InputStream resource used to read the '" +
                                      propertiesFileName + "' file");
                }
            }
        }
        return iotServerConfigs;
    }

    /**
     * This method constructs the URLs for each of the API Endpoints called by the device agent
     * Ex: Register API, Push-Data API
     */
    public static void initializeServerEndPoints() {
        AgentManager agentManager = AgentManager.getInstance();
        String serverSecureEndpoint = agentManager.getAgentConfigs().getHTTPS_ServerEndpoint();
        String serverUnSecureEndpoint = agentManager.getAgentConfigs().getHTTP_ServerEndpoint();
        String backEndContext = agentManager.getAgentConfigs().getControllerContext();
        String scepBackEndContext = agentManager.getAgentConfigs().getScepContext();

        String deviceControllerAPIEndpoint = serverSecureEndpoint + backEndContext;

        String deviceEnrollmentEndpoint =
                serverUnSecureEndpoint + scepBackEndContext + AgentConstants.DEVICE_ENROLLMENT_API_EP;
        agentManager.setEnrollmentEP(deviceEnrollmentEndpoint);

        String registerEndpointURL =
                deviceControllerAPIEndpoint + AgentConstants.DEVICE_REGISTER_API_EP;
        agentManager.setIpRegistrationEP(registerEndpointURL);

        String pushDataEndPointURL =
                deviceControllerAPIEndpoint + AgentConstants.DEVICE_PUSH_TEMPERATURE_API_EP;
        agentManager.setPushDataAPIEP(pushDataEndPointURL);

        log.info(AgentConstants.LOG_APPENDER + "IoT Server's Device Controller API Endpoint: " +
                         deviceControllerAPIEndpoint);
        log.info(AgentConstants.LOG_APPENDER + "Device Enrollment EndPoint: " +
                         registerEndpointURL);
        log.info(AgentConstants.LOG_APPENDER + "DeviceIP Registration EndPoint: " +
                         registerEndpointURL);
        log.info(AgentConstants.LOG_APPENDER + "Push-Data API EndPoint: " + pushDataEndPointURL);
    }

    public static void setHTTPSConfigurations() {
        String apimEndpoint = AgentManager.getInstance().getAgentConfigs().getApimGatewayEndpoint();
        System.setProperty("javax.net.ssl.trustStore", AgentConstants.DEVICE_KEYSTORE);
        System.setProperty("javax.net.ssl.trustStorePassword", AgentConstants.DEVICE_KEYSTORE_PASSWORD);

        try {
            final String apimHost = TransportUtils.getHostAndPort(apimEndpoint).get(AgentConstants.HOST_PROPERTY);

            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return hostname.equals(apimHost);
                }
            });
        } catch (TransportHandlerException e) {
            log.error(AgentConstants.LOG_APPENDER +
                              "Failed to set HTTPS HostNameVerifier to the APIMServer-Host using the APIM-Endpoint " +
                              "string [" + apimEndpoint + "].");
            log.error(AgentConstants.LOG_APPENDER + e);
        }
    }

    public static String prepareSecurePayLoad(String message) throws AgentCoreOperationException {
        PrivateKey devicePrivateKey = EnrollmentManager.getInstance().getPrivateKey();
        String encodedMessage = Base64.encodeBase64String(message.getBytes());
        String signedPayload;
        try {
            signedPayload = CommunicationUtils.signMessage(encodedMessage, devicePrivateKey);
        } catch (TransportHandlerException e) {
            String errorMsg = "Error occurred whilst trying to sign encrypted message of: [" + message + "]";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        }

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put(JSON_MESSAGE_KEY, encodedMessage);
        jsonPayload.put(JSON_SIGNATURE_KEY, signedPayload);
        //below statements are temporary fix.
        jsonPayload.put(JSON_SERIAL_KEY, EnrollmentManager.getInstance().getSCEPCertificate().getSerialNumber());
        return jsonPayload.toString();
    }

    public static String extractMessageFromPayload(String message) throws AgentCoreOperationException {
        String actualMessage;

        PublicKey serverPublicKey = EnrollmentManager.getInstance().getServerPublicKey();
        JSONObject jsonPayload = new JSONObject(message);
        Object encodedMessage = jsonPayload.get(JSON_MESSAGE_KEY);
        Object signedPayload = jsonPayload.get(JSON_SIGNATURE_KEY);
        boolean verification;

        if (encodedMessage != null && signedPayload != null) {
            try {
                verification = CommunicationUtils.verifySignature(
                        encodedMessage.toString(), signedPayload.toString(), serverPublicKey);
            } catch (TransportHandlerException e) {
                String errorMsg =
                        "Error occurred whilst trying to verify signature on received message: [" + message + "]";
                log.error(errorMsg);
                throw new AgentCoreOperationException(errorMsg, e);
            }
        } else {
            String errorMsg = "The received message is in an INVALID format. " +
                    "Need to be JSON - {\"Msg\":\"<ENCRYPTED_MSG>\", \"Sig\":\"<SIGNED_MSG>\"}.";
            throw new AgentCoreOperationException(errorMsg);
        }
        if (verification) {
            actualMessage = new String(Base64.decodeBase64(encodedMessage.toString()), StandardCharsets.UTF_8);
        } else {
            String errorMsg = "Could not verify payload signature. The message was not signed by a valid client";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg);
        }
        return actualMessage;
    }

    public static String getAuthenticationMethod() {
        String authMethod = AgentManager.getInstance().getAgentConfigs().getAuthMethod();
        switch (authMethod) {
            case AgentConstants.TOKEN_AUTHENTICATION_METHOD:
                return AgentConstants.TOKEN_AUTHENTICATION_METHOD;
            default:
                return "";
        }
    }

    public static void refreshOAuthToken() throws AgentCoreOperationException {

        AgentManager agentManager = AgentManager.getInstance();
        String tokenEndpoint = agentManager.getAgentConfigs().getApimGatewayEndpoint();
        tokenEndpoint = tokenEndpoint + APIManagerTokenUtils.TOKEN_ENDPOINT;

        HttpURLConnection httpConnection = null;
        BufferedReader connectionBuffer = null;
        String requestPayload;
        String dataFromBuffer;
        StringBuilder responseMessage = new StringBuilder();

        try {
            String refreshToken = agentManager.getAgentConfigs().getRefreshToken();
            String applicationScope = "device_type_" + AgentConstants.DEVICE_TYPE +
                    " device_" + agentManager.getAgentConfigs().getDeviceId();

            requestPayload = APIManagerTokenUtils.GRANT_TYPE + "=" + APIManagerTokenUtils.REFRESH_TOKEN + "&" +
                    APIManagerTokenUtils.REFRESH_TOKEN + "=" + refreshToken + "&" +
                    APIManagerTokenUtils.SCOPE + "=" + applicationScope;

            httpConnection = TransportUtils.getHttpConnection(tokenEndpoint);
            httpConnection.setRequestMethod(AgentConstants.HTTP_POST);
            httpConnection.setRequestProperty(AgentConstants.AUTHORIZATION_HEADER,
                                              "Basic " + agentManager.getAgentConfigs().getApiApplicationKey());
            httpConnection.setRequestProperty(AgentConstants.CONTENT_TYPE_HEADER, AgentConstants.X_WWW_FORM_URLENCODED);
            httpConnection.setDoOutput(true);

            DataOutputStream dataOutPutWriter = new DataOutputStream(httpConnection.getOutputStream());
            dataOutPutWriter.writeBytes(requestPayload);
            dataOutPutWriter.flush();
            dataOutPutWriter.close();

            log.info(AgentConstants.LOG_APPENDER + "Request to refresh OAuth token was sent to [" +
                             httpConnection.getURL() + "] with payload [" + requestPayload + "].");
            log.info(AgentConstants.LOG_APPENDER + "Response [" + httpConnection.getResponseCode() + ":" +
                             httpConnection.getResponseMessage() + "] was received for token refresh attempt.");

            if (httpConnection.getResponseCode() == HttpStatus.OK_200) {
                connectionBuffer = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                while ((dataFromBuffer = connectionBuffer.readLine()) != null) {
                    responseMessage.append(dataFromBuffer);
                }

                log.info(AgentConstants.LOG_APPENDER +
                                 "Response " + responseMessage + " was received for the token refresh call.");
                updateExistingTokens(responseMessage.toString());
            } else if (httpConnection.getResponseCode() == HttpStatus.BAD_REQUEST_400) {
                log.error(AgentConstants.LOG_APPENDER +
                                  "Token refresh call returned with a [400 Bad Request].\nThe refresh-token has " +
                                  "probably expired.\nPlease contact System-Admin to get a valid refresh-token.");
            } else {
                log.warn(AgentConstants.LOG_APPENDER + "There was an issue with refreshing the Access Token.");
            }

        } catch (TransportHandlerException e) {
            throw new AgentCoreOperationException(e);
        } catch (ProtocolException e) {
            String errorMsg = "Protocol specific error occurred when trying to set method to " +
                    AgentConstants.HTTP_POST + " for endpoint at: " + tokenEndpoint;
            log.error(AgentConstants.LOG_APPENDER + errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);

        } catch (IOException e) {
            String errorMsg = "An IO error occurred whilst trying to get the response code from: " + tokenEndpoint +
                    " for a HTTP " + AgentConstants.HTTP_POST + " call.";
            log.error(AgentConstants.LOG_APPENDER + errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        } finally {
            if (connectionBuffer != null) {
                try {
                    connectionBuffer.close();
                } catch (IOException e) {
                    log.error(AgentConstants.LOG_APPENDER +
                                      "Error encounter whilst attempting to close buffer to connection at: " +
                                      tokenEndpoint);
                }
            }

            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    private static void updateExistingTokens(String responseFromTokenEP) throws AgentCoreOperationException {
        JSONObject jsonTokenObject = new JSONObject(responseFromTokenEP);
        String newAccessToken = jsonTokenObject.get(APIManagerTokenUtils.ACCESS_TOKEN).toString();
        String newRefreshToken = jsonTokenObject.get(APIManagerTokenUtils.REFRESH_TOKEN).toString();

        if (newAccessToken == null || newRefreshToken == null) {
            String msg =
                    "Neither Access-Token nor Refresh-Token was found in the response [" + responseFromTokenEP + "].";
            log.error(AgentConstants.LOG_APPENDER + msg);
            throw new AgentCoreOperationException(msg);
        }

        AgentManager.getInstance().getAgentConfigs().setAuthToken(newAccessToken);
        AgentManager.getInstance().getAgentConfigs().setRefreshToken(newRefreshToken);
        String deviceConfigFilePath =
                AgentManager.getInstance().getRootPath() + AgentConstants.AGENT_PROPERTIES_FILE_NAME;

        try {
            PropertiesConfiguration propertyFileConfiguration = new PropertiesConfiguration(deviceConfigFilePath);
            propertyFileConfiguration.setProperty(AgentConstants.AUTH_TOKEN_PROPERTY, newAccessToken);
            propertyFileConfiguration.setProperty(AgentConstants.REFRESH_TOKEN_PROPERTY, newRefreshToken);
            propertyFileConfiguration.save();
        } catch (ConfigurationException e) {
            String msg = "Error occurred whilst trying to update the [" + AgentConstants.AGENT_PROPERTIES_FILE_NAME +
                    "] at: " + deviceConfigFilePath + " will the new tokens.";
            log.error(AgentConstants.LOG_APPENDER + msg);
            throw new AgentCoreOperationException(msg);
        }
    }

    private class APIManagerTokenUtils {
        public static final String TOKEN_ENDPOINT = "/oauth2/token";
        public static final String GRANT_TYPE = "grant_type";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String SCOPE = "scope";
    }

}

