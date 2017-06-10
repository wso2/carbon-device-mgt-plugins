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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.advanced.core;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.advanced.enrollment.EnrollmentManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.advanced.exception.AgentCoreOperationException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.advanced.transport.CommunicationUtils;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.advanced.transport.TransportHandlerException;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
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
    public static AgentConfiguration readIoTServerConfigs() {
        AgentManager agentManager = AgentManager.getInstance();
        AgentConfiguration iotServerConfigs = new AgentConfiguration();
        Properties properties = new Properties();
        InputStream propertiesInputStream = null;
        String propertiesFileName = AgentConstants.AGENT_PROPERTIES_FILE_NAME;

        try {
            ClassLoader loader = AgentUtilOperations.class.getClassLoader();
            URL path = loader.getResource(propertiesFileName);
            log.info(AgentConstants.LOG_APPENDER + path);
            String rootPath = path.getPath().replace("wso2-firealarm-virtual-agent-advanced.jar!/deviceConfig"
                    + ".properties", "").replace("jar:", "").replace("file:", "");

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
            iotServerConfigs.setServerJID(properties.getProperty(
                    AgentConstants.SERVER_JID_PROPERTY));
            iotServerConfigs.setDeviceName(properties.getProperty(
                    AgentConstants.DEVICE_NAME_PROPERTY));
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
            log.info(AgentConstants.LOG_APPENDER + "Authentication Token: " +
                             iotServerConfigs.getAuthToken());
            log.info(AgentConstants.LOG_APPENDER + "Refresh Token: " +
                             iotServerConfigs.getRefreshToken());
            log.info(AgentConstants.LOG_APPENDER + "Data Push Interval: " +
                             iotServerConfigs.getDataPushInterval());
            log.info(AgentConstants.LOG_APPENDER + "XMPP Server Name: " +
                             iotServerConfigs.getXmppServerName());

        } catch (FileNotFoundException ex) {
            log.error(AgentConstants.LOG_APPENDER + "Unable to find " + propertiesFileName +
                              " file at: " + AgentConstants.PROPERTIES_FILE_PATH);
            iotServerConfigs = setDefaultDeviceConfigs();

        } catch (IOException ex) {
            log.error(AgentConstants.LOG_APPENDER + "Error occurred whilst trying to fetch '" +
                              propertiesFileName + "' from: " +
                              AgentConstants.PROPERTIES_FILE_PATH);
            iotServerConfigs = setDefaultDeviceConfigs();

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
     * Sets the default Device specific configurations listed in the 'AgentConstants' class.
     *
     * @return an object of AgentConfiguration class including all default device specific configs.
     */
    private static AgentConfiguration setDefaultDeviceConfigs() {
        log.warn(AgentConstants.LOG_APPENDER +
                         "Default Values are being set to all Agent specific configurations");

        AgentConfiguration iotServerConfigs = new AgentConfiguration();

        iotServerConfigs.setDeviceOwner(AgentConstants.DEFAULT_SERVER_NAME);
        iotServerConfigs.setDeviceOwner(AgentConstants.DEFAULT_DEVICE_OWNER);
        iotServerConfigs.setDeviceId(AgentConstants.DEFAULT_DEVICE_ID);
        iotServerConfigs.setServerJID(AgentConstants.DEFAULT_SERVER_JID);
        iotServerConfigs.setDeviceName(AgentConstants.DEFAULT_DEVICE_NAME);
        iotServerConfigs.setScepContext(AgentConstants.DEVICE_SCEP_API_EP);
        iotServerConfigs.setHTTPS_ServerEndpoint(AgentConstants.DEFAULT_HTTPS_SERVER_EP);
        iotServerConfigs.setHTTP_ServerEndpoint(AgentConstants.DEFAULT_HTTP_SERVER_EP);
        iotServerConfigs.setApimGatewayEndpoint(AgentConstants.DEFAULT_APIM_GATEWAY_EP);
        iotServerConfigs.setMqttBrokerEndpoint(AgentConstants.DEFAULT_MQTT_BROKER_EP);
        iotServerConfigs.setXmppServerEndpoint(AgentConstants.DEFAULT_XMPP_SERVER_EP);
        iotServerConfigs.setAuthToken(AgentConstants.DEFAULT_AUTH_TOKEN);
        iotServerConfigs.setRefreshToken(AgentConstants.DEFAULT_REFRESH_TOKEN);
        iotServerConfigs.setDataPushInterval(AgentConstants.DEFAULT_DATA_PUBLISH_INTERVAL);

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
        String backEndContext = "/virtual_firealarm/device";
        String scepBackEndContext = "/virtual_firealarm_scep";
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


    public static String prepareSecurePayLoad(String message) throws AgentCoreOperationException {
        if (EnrollmentManager.getInstance().isEnrolled()) {
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
        } else {
            return message;
        }
    }


    public static String extractMessageFromPayload(String message) throws AgentCoreOperationException {
        if (EnrollmentManager.getInstance().isEnrolled()) {
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
        } else {
            return message;
        }
    }

    public static String formatMessage(String message) {
        StringBuilder formattedMsg = new StringBuilder(message);

        ArrayList<String> keyWordList = new ArrayList<String>();
        keyWordList.add("define");
        keyWordList.add("from");
        keyWordList.add("select");
        keyWordList.add("group");
        keyWordList.add("insert");
        keyWordList.add(";");


        for (String keyWord : keyWordList) {
            int startIndex = 0;

            while (true) {
                int keyWordIndex = formattedMsg.indexOf(keyWord, startIndex);

                if (keyWordIndex == -1) {
                    break;
                }

                if (keyWord.equals(";")) {
                    if (keyWordIndex != 0 && (keyWordIndex + 1) != formattedMsg.length() &&
                            formattedMsg.charAt(keyWordIndex + 1) == ' ') {
                        formattedMsg.setCharAt((keyWordIndex + 1), '\n');
                    }
                } else {
                    if (keyWordIndex != 0 && formattedMsg.charAt(keyWordIndex - 1) == ' ') {
                        formattedMsg.setCharAt((keyWordIndex - 1), '\n');
                    }
                }
                startIndex = keyWordIndex + 1;
            }
        }
        return formattedMsg.toString();
    }

    public static boolean writeToFile(String content, String fileLocation) {
        File file = new File(fileLocation);

        try (FileOutputStream fop = new FileOutputStream(file)) {

            // if file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // get the content in bytes
            byte[] contentInBytes = content.getBytes(StandardCharsets.UTF_8);

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

            System.out.println("Done");
            AgentManager.setUpdated(true);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}

