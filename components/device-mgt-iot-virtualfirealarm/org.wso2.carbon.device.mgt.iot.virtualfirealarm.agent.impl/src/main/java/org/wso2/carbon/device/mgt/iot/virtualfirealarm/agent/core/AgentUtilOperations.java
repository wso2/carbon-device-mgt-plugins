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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.enrollment.EnrollmentManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.exception.AgentCoreOperationException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.transport.CommunicationUtils;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.transport.TransportHandlerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

        try {
            ClassLoader loader = AgentUtilOperations.class.getClassLoader();
            URL path = loader.getResource(propertiesFileName);
            System.out.println(path);
            String root = path.getPath().replace("wso2-firealarm-virtual-agent.jar!/deviceConfig.properties", "")
                                        .replace("jar:", "").replace("file:", "");

            root = URLDecoder.decode(root, StandardCharsets.UTF_8.toString());
            agentManager.setRootPath(root);

            String deviceConfigFilePath = root + AgentConstants.AGENT_PROPERTIES_FILE_NAME;
            propertiesInputStream = new FileInputStream(deviceConfigFilePath);

            //load a properties file from class path, inside static method
            properties.load(propertiesInputStream);

            iotServerConfigs.setServerName(properties.getProperty(
                    AgentConstants.SERVER_NAME_PROPERTY));
            iotServerConfigs.setDeviceOwner(properties.getProperty(
                    AgentConstants.DEVICE_OWNER_PROPERTY));
            iotServerConfigs.setDeviceId(properties.getProperty(
                    AgentConstants.DEVICE_ID_PROPERTY));
            iotServerConfigs.setDeviceName(properties.getProperty(
                    AgentConstants.DEVICE_NAME_PROPERTY));
            iotServerConfigs.setControllerContext(properties.getProperty(
                    AgentConstants.DEVICE_CONTROLLER_CONTEXT_PROPERTY));
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
            iotServerConfigs.setAuthMethod(properties.getProperty(
                    AgentConstants.AUTH_METHOD_PROPERTY));
            iotServerConfigs.setAuthToken(properties.getProperty(
                    AgentConstants.AUTH_TOKEN_PROPERTY));
            iotServerConfigs.setRefreshToken(properties.getProperty(
                    AgentConstants.REFRESH_TOKEN_PROPERTY));
            iotServerConfigs.setDataPushInterval(Integer.parseInt(properties.getProperty(
                    AgentConstants.PUSH_INTERVAL_PROPERTY)));

            log.info(AgentConstants.LOG_APPENDER + "Server name: " +
                             iotServerConfigs.getServerName());
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
            log.info(AgentConstants.LOG_APPENDER + "Authentication Token: " +
                             iotServerConfigs.getAuthToken());
            log.info(AgentConstants.LOG_APPENDER + "Refresh Token: " +
                             iotServerConfigs.getRefreshToken());
            log.info(AgentConstants.LOG_APPENDER + "Data Push Interval: " +
                             iotServerConfigs.getDataPushInterval());

        } catch (FileNotFoundException ex) {
            String errorMsg = "[" + propertiesFileName + "] file not found at: " + AgentConstants.PROPERTIES_FILE_PATH;
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
     *
     * @throws AgentCoreOperationException if any error occurs at socket level whilst trying to
     *                                     retrieve the deviceIP of the network-interface read
     *                                     from the configs file
     */
    public static void initializeServerEndPoints() {
        AgentManager agentManager = AgentManager.getInstance();
        String serverSecureEndpoint = agentManager.getAgentConfigs().getHTTPS_ServerEndpoint();
        String serverUnSecureEndpoint = agentManager.getAgentConfigs().getHTTP_ServerEndpoint();
        String backEndContext = agentManager.getAgentConfigs().getControllerContext();

        String deviceControllerAPIEndpoint = serverSecureEndpoint + backEndContext;

        String deviceEnrollmentEndpoint =
                serverUnSecureEndpoint + backEndContext + AgentConstants.DEVICE_ENROLLMENT_API_EP;
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


    public static String prepareSecurePayLoad(String message) throws AgentCoreOperationException {
        PublicKey serverPublicKey = EnrollmentManager.getInstance().getServerPublicKey();
        PrivateKey devicePrivateKey = EnrollmentManager.getInstance().getPrivateKey();

        String encryptedMsg;
        try {
            encryptedMsg = CommunicationUtils.encryptMessage(message, serverPublicKey);
        } catch (TransportHandlerException e) {
            String errorMsg = "Error occurred whilst trying to encryptMessage: [" + message + "]";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        }

        String signedPayload;
        try {
            signedPayload = CommunicationUtils.signMessage(encryptedMsg, devicePrivateKey);
        } catch (TransportHandlerException e) {
            String errorMsg = "Error occurred whilst trying to sign encrypted message of: [" + message + "]";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        }

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put(JSON_MESSAGE_KEY, encryptedMsg);
        jsonPayload.put(JSON_SIGNATURE_KEY, signedPayload);

        return jsonPayload.toString();
    }


    public static String extractMessageFromPayload(String message) throws AgentCoreOperationException {
        String actualMessage;

        PublicKey serverPublicKey = EnrollmentManager.getInstance().getServerPublicKey();
        PrivateKey devicePrivateKey = EnrollmentManager.getInstance().getPrivateKey();

        JSONObject jsonPayload = new JSONObject(message);
        Object encryptedMessage = jsonPayload.get(JSON_MESSAGE_KEY);
        Object signedPayload = jsonPayload.get(JSON_SIGNATURE_KEY);
        boolean verification;

        if (encryptedMessage != null && signedPayload != null) {
            try {
                verification = CommunicationUtils.verifySignature(
                        encryptedMessage.toString(), signedPayload.toString(), serverPublicKey);
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

        try {
            if (verification) {
                actualMessage = CommunicationUtils.decryptMessage(encryptedMessage.toString(), devicePrivateKey);
            } else {
                String errorMsg = "Could not verify payload signature. The message was not signed by a valid client";
                log.error(errorMsg);
                throw new AgentCoreOperationException(errorMsg);
            }
        } catch (TransportHandlerException e) {
            String errorMsg = "Error occurred whilst trying to decrypt received message: [" + encryptedMessage + "]";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        }

        return actualMessage;
    }


}

