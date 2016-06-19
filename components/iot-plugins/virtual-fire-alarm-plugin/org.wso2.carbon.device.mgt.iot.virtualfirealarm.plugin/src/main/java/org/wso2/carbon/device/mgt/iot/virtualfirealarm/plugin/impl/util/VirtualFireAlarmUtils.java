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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.device.mgt.iot.devicetype.config.DeviceManagementConfiguration;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.exception.VirtualFirealarmDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.internal.VirtualFirealarmManagementDataHolder;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.xmpp.XmppConfig;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.MessageType;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.json.JSONObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains utility methods used by FireAlarm plugin.
 */
public class VirtualFireAlarmUtils {

    private static Log log = LogFactory.getLog(VirtualFireAlarmUtils.class);

    public static void cleanupResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing result set", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing prepared statement", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing database connection", e);
            }
        }
    }

    public static void cleanupResources(PreparedStatement stmt, ResultSet rs) {
        cleanupResources(null, stmt, rs);
    }

    /**
     * Creates the device management schema.
     */
    public static void setupDeviceManagementSchema() throws VirtualFirealarmDeviceMgtPluginException {
        DeviceManagementConfiguration deviceManagementConfiguration = VirtualFirealarmManagementDataHolder.getInstance()
                .getDeviceTypeConfigService().getConfiguration(VirtualFireAlarmConstants.DEVICE_TYPE,
                                                               VirtualFireAlarmConstants.DEVICE_TYPE_PROVIDER_DOMAIN);
        String datasourceName = deviceManagementConfiguration.getDeviceManagementConfigRepository()
                .getDataSourceConfig().getJndiLookupDefinition().getJndiName();
        try {
            Context ctx = new InitialContext();
            DataSource dataSource = (DataSource) ctx.lookup(datasourceName);
            DeviceSchemaInitializer initializer = new DeviceSchemaInitializer(dataSource);
            log.info("Initializing device management repository database schema");
            initializer.createRegistryDatabase();
        } catch (NamingException e) {
            log.error("Error while looking up the data source: " + datasourceName, e);
        } catch (Exception e) {
            throw new VirtualFirealarmDeviceMgtPluginException("Error occurred while initializing Iot Device " +
                                                                       "Management database schema", e);
        }
    }

    public static void setupMqttInputAdapter() throws IOException {
        if (!MqttConfig.getInstance().isEnabled()) {
            return;
        }
        InputEventAdapterConfiguration inputEventAdapterConfiguration =
                createMqttInputEventAdapterConfiguration(VirtualFireAlarmConstants.MQTT_ADAPTER_NAME,
                                                         VirtualFireAlarmConstants.MQTT_ADAPTER_TYPE, MessageType.TEXT);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    VirtualFireAlarmConstants.DEVICE_TYPE_PROVIDER_DOMAIN, true);
            VirtualFirealarmManagementDataHolder.getInstance().getInputEventAdapterService()
                    .create(inputEventAdapterConfiguration, new VirtualFirealarmEventAdapterSubscription());
        } catch (InputEventAdapterException e) {
            log.error("Unable to create Input Event Adapter : " + VirtualFireAlarmConstants.MQTT_ADAPTER_NAME, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Create Output Event Adapter Configuration for given configuration.
     *
     * @param name      Input Event Adapter name
     * @param type      Input Event Adapter type
     * @param msgFormat Input Event Adapter message format
     * @return InputEventAdapterConfiguration instance for given configuration
     */
    private static InputEventAdapterConfiguration createMqttInputEventAdapterConfiguration(String name, String type,
                                                    String msgFormat) throws IOException {
        InputEventAdapterConfiguration inputEventAdapterConfiguration = new InputEventAdapterConfiguration();
        inputEventAdapterConfiguration.setName(name);
        inputEventAdapterConfiguration.setType(type);
        inputEventAdapterConfiguration.setMessageFormat(msgFormat);
        Map<String, String> mqttAdapterProperties = new HashMap<>();
        mqttAdapterProperties.put(VirtualFireAlarmConstants.USERNAME_PROPERTY_KEY, MqttConfig.getInstance().getUsername());
        mqttAdapterProperties.put(VirtualFireAlarmConstants.DCR_PROPERTY_KEY, MqttConfig.getInstance().getDcrUrl());
        mqttAdapterProperties.put(VirtualFireAlarmConstants.BROKER_URL_PROPERTY_KEY, MqttConfig.getInstance().getUrl());
        mqttAdapterProperties.put(VirtualFireAlarmConstants.SCOPES_PROPERTY_KEY, MqttConfig.getInstance().getScopes());
        mqttAdapterProperties.put(VirtualFireAlarmConstants.CLEAR_SESSION_PROPERTY_KEY, MqttConfig.getInstance()
                .getClearSession());
        mqttAdapterProperties.put(VirtualFireAlarmConstants.QOS_PROPERTY_KEY, MqttConfig.getInstance().getQos());
        mqttAdapterProperties.put(VirtualFireAlarmConstants.CLIENT_ID_PROPERTY_KEY, "");
        mqttAdapterProperties.put(VirtualFireAlarmConstants.TOPIC, VirtualFireAlarmConstants.SUBSCRIBED_TOPIC);
        mqttAdapterProperties.put(VirtualFireAlarmConstants.CONTENT_TRANSFORMATION,
                                  VirtualFirealarmMqttContentTransformer.class.getName());
        mqttAdapterProperties.put(VirtualFireAlarmConstants.CONTENT_VALIDATION, "default");
        mqttAdapterProperties.put(VirtualFireAlarmConstants.RESOURCE, "input-event");
        inputEventAdapterConfiguration.setProperties(mqttAdapterProperties);

        return inputEventAdapterConfiguration;
    }

    public static String extractMessageFromPayload(String message, PublicKey verifySignatureKey)
            throws VirtualFirealarmDeviceMgtPluginException {
        String actualMessage;

        JSONObject jsonPayload = new JSONObject(message);
        Object encodedMessage = jsonPayload.get(VirtualFireAlarmConstants.JSON_MESSAGE_KEY);
        Object signedPayload = jsonPayload.get(VirtualFireAlarmConstants.JSON_SIGNATURE_KEY);

        if (encodedMessage != null && signedPayload != null) {
            if (VirtualFirealarmSecurityManager.verifySignature(
                    encodedMessage.toString(), signedPayload.toString(), verifySignatureKey)) {
                actualMessage = new String(Base64.decodeBase64(encodedMessage.toString()));
                //VirtualFirealarmSecurityManager.decryptMessage(encryptedMessage.toString(), decryptionKey);
            } else {
                String errorMsg = "The message was not signed by a valid client. Could not verify signature on payload";
                throw new VirtualFirealarmDeviceMgtPluginException(errorMsg);
            }
        } else {
            String errorMsg = "The received message is in an INVALID format. " +
                    "Need to be JSON - {\"Msg\":\"<ENCRYPTED_MSG>\", \"Sig\":\"<SIGNED_MSG>\"}.";
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg);
        }

        return actualMessage;
    }

    public static PublicKey getDevicePublicKey(String alias) throws VirtualFirealarmDeviceMgtPluginException {
        PublicKey clientPublicKey;
        try {
            CertificateManagementService certificateManagementService =
                    VirtualFirealarmManagementDataHolder.getInstance().getCertificateManagementService();
            X509Certificate clientCertificate = (X509Certificate) certificateManagementService.getCertificateByAlias(
                    alias);
            clientPublicKey = clientCertificate.getPublicKey();
        } catch (KeystoreException e) {
            String errorMsg;
            if (e.getMessage().contains("NULL_CERT")) {
                errorMsg = "The Device-View page might have been accessed prior to the device being started.";
                if(log.isDebugEnabled()){
                    log.debug(errorMsg);
                }
                throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
            } else {
                errorMsg = "An error occurred whilst trying to retrieve certificate for alias [" + alias +
                        "] with alias: [" + alias + "]";
                if(log.isDebugEnabled()){
                    log.debug(errorMsg);
                }
                throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
            }
        }
        return clientPublicKey;
    }

    public static boolean publishToDAS(String deviceId, float temperature) {
        EventsPublisherService deviceAnalyticsService =
                VirtualFirealarmManagementDataHolder.getInstance().getEventsPublisherService();
        if (deviceAnalyticsService != null) {
            String owner = "";
            Object metdaData[] = {owner, VirtualFireAlarmConstants.DEVICE_TYPE, deviceId, System.currentTimeMillis()};
            Object payloadData[] = {temperature};
            try {
                deviceAnalyticsService.publishEvent(VirtualFireAlarmConstants.TEMPERATURE_STREAM_DEFINITION,
                                                    "1.0.0", metdaData, new Object[0], payloadData);
            } catch (DataPublisherConfigurationException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static void setupXmppInputAdapter() throws IOException {
        if (!XmppConfig.getInstance().isEnabled()) return;
        InputEventAdapterConfiguration inputEventAdapterConfiguration =
                createXmppInputEventAdapterConfiguration(VirtualFireAlarmConstants.XMPP_ADAPTER_NAME,
                                                         VirtualFireAlarmConstants.XMPP_ADAPTER_TYPE, MessageType.TEXT);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    VirtualFireAlarmConstants.DEVICE_TYPE_PROVIDER_DOMAIN, true);
            VirtualFirealarmManagementDataHolder.getInstance().getInputEventAdapterService()
                    .create(inputEventAdapterConfiguration, new VirtualFirealarmEventAdapterSubscription());
        } catch (InputEventAdapterException e) {
            log.error("Unable to create Input Event Adapter : " + VirtualFireAlarmConstants.MQTT_ADAPTER_NAME, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Create Input Event Adapter Configuration for given configuration.
     *
     * @param name      Input Event Adapter name
     * @param type      Input Event Adapter type
     * @param msgFormat Input Event Adapter message format
     * @return InputEventAdapterConfiguration instance for given configuration
     */
    private static InputEventAdapterConfiguration createXmppInputEventAdapterConfiguration(String name, String type,
                                                                                           String msgFormat) throws IOException {
        InputEventAdapterConfiguration inputEventAdapterConfiguration = new InputEventAdapterConfiguration();
        inputEventAdapterConfiguration.setName(name);
        inputEventAdapterConfiguration.setType(type);
        inputEventAdapterConfiguration.setMessageFormat(msgFormat);
        Map<String, String> xmppAdapterProperties = new HashMap<>();
        XmppConfig xmppConfig = XmppConfig.getInstance();
        xmppAdapterProperties.put(VirtualFireAlarmConstants.HOST_KEY, xmppConfig.getHost());
        xmppAdapterProperties.put(VirtualFireAlarmConstants.PORT_KEY, String.valueOf(xmppConfig.getPort()));
        xmppAdapterProperties.put(VirtualFireAlarmConstants.USERNAME_PROPERTY_KEY, xmppConfig.getUsername());
        xmppAdapterProperties.put(VirtualFireAlarmConstants.PASSWORD_PROPERTY_KEY, xmppConfig.getPassword());
        xmppAdapterProperties.put(VirtualFireAlarmConstants.JID_PROPERTY_KEY, xmppConfig.getJid());
        xmppAdapterProperties.put(VirtualFireAlarmConstants.CONTENT_TRANSFORMATION,
                                  VirtualFirealarmXmppContentTransformer.class.getName());
        xmppAdapterProperties.put(VirtualFireAlarmConstants.CONTENT_VALIDATION, "default");
        inputEventAdapterConfiguration.setProperties(xmppAdapterProperties);
        return inputEventAdapterConfiguration;
    }

}
