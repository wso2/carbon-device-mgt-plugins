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

package org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.constants.RaspberrypiConstants;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.exception.RaspberrypiDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.internal.RaspberrypiManagementDataHolder;
import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Contains utility methods used by Raspberrypi plugin.
 */
public class RaspberrypiUtils {

    private static Log log = LogFactory.getLog(RaspberrypiUtils.class);

    private static final String VIRTUAL_FIREALARM_CONFIG_LOCATION =
            CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "conf" +
                    File.separator + "iot" + File.separator + "mqtt.properties";

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
    public static void setupDeviceManagementSchema() throws RaspberrypiDeviceMgtPluginException {
        try {
            Context ctx = new InitialContext();
            DataSource dataSource = (DataSource) ctx.lookup(RaspberrypiConstants.DATA_SOURCE_NAME);
            DeviceSchemaInitializer initializer =
                    new DeviceSchemaInitializer(dataSource);
            log.info("Initializing device management repository database schema");
            initializer.createRegistryDatabase();
        } catch (NamingException e) {
            log.error("Error while looking up the data source: " + RaspberrypiConstants.DATA_SOURCE_NAME);
        } catch (Exception e) {
            throw new RaspberrypiDeviceMgtPluginException("Error occurred while initializing Iot Device " +
                                                                     "Management database schema", e);
        }
    }

    public static void setupMqttOutputAdapter() throws IOException {
        OutputEventAdapterConfiguration outputEventAdapterConfiguration =
                createMqttOutputEventAdapterConfiguration(RaspberrypiConstants.MQTT_ADAPTER_NAME,
                                                          RaspberrypiConstants.MQTT_ADAPTER_TYPE, MessageType.TEXT);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    RaspberrypiConstants.DEVICE_TYPE_PROVIDER_DOMAIN, true);
            RaspberrypiManagementDataHolder.getInstance().getOutputEventAdapterService()
                    .create(outputEventAdapterConfiguration);
        } catch (OutputEventAdapterException e) {
            log.error("Unable to create Output Event Adapter : " + RaspberrypiConstants.MQTT_ADAPTER_NAME, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Create Output Event Adapter Configuration for given configuration.
     *
     * @param name      Output Event Adapter name
     * @param type      Output Event Adapter type
     * @param msgFormat Output Event Adapter message format
     * @return OutputEventAdapterConfiguration instance for given configuration
     */
    private static OutputEventAdapterConfiguration createMqttOutputEventAdapterConfiguration(String name, String type,
                                                                                             String msgFormat) throws IOException {
        OutputEventAdapterConfiguration outputEventAdapterConfiguration = new OutputEventAdapterConfiguration();
        outputEventAdapterConfiguration.setName(name);
        outputEventAdapterConfiguration.setType(type);
        outputEventAdapterConfiguration.setMessageFormat(msgFormat);
        File configFile = new File(VIRTUAL_FIREALARM_CONFIG_LOCATION);
        if (configFile.exists()) {
            Map<String, String> mqttAdapterProperties = new HashMap<>();
            InputStream propertyStream = configFile.toURI().toURL().openStream();
            Properties properties = new Properties();
            properties.load(propertyStream);
            mqttAdapterProperties.put(RaspberrypiConstants.USERNAME_PROPERTY_KEY, properties.getProperty(
                    RaspberrypiConstants.USERNAME_PROPERTY_KEY));
            mqttAdapterProperties.put(RaspberrypiConstants.DCR_PROPERTY_KEY, Utils.replaceSystemProperty(
                    properties.getProperty(RaspberrypiConstants.DCR_PROPERTY_KEY)));
            mqttAdapterProperties.put(RaspberrypiConstants.BROKER_URL_PROPERTY_KEY, replaceMqttProperty(
                    properties.getProperty(RaspberrypiConstants.BROKER_URL_PROPERTY_KEY)));
            mqttAdapterProperties.put(RaspberrypiConstants.SCOPES_PROPERTY_KEY, properties.getProperty(
                    RaspberrypiConstants.SCOPES_PROPERTY_KEY));
            mqttAdapterProperties.put(RaspberrypiConstants.CLEAR_SESSION_PROPERTY_KEY, properties.getProperty(
                    RaspberrypiConstants.CLEAR_SESSION_PROPERTY_KEY));
            mqttAdapterProperties.put(RaspberrypiConstants.QOS_PROPERTY_KEY, properties.getProperty(
                    RaspberrypiConstants.QOS_PROPERTY_KEY));
            mqttAdapterProperties.put(RaspberrypiConstants.CLIENT_ID_PROPERTY_KEY, "");
            outputEventAdapterConfiguration.setStaticProperties(mqttAdapterProperties);
        }
        return outputEventAdapterConfiguration;
    }

    private static String replaceMqttProperty(String urlWithPlaceholders) {
        urlWithPlaceholders = Utils.replaceSystemProperty(urlWithPlaceholders);
        urlWithPlaceholders = urlWithPlaceholders.replaceAll(RaspberrypiConstants.MQTT_PORT, "" +
                (RaspberrypiConstants.DEFAULT_MQTT_PORT + getPortOffset()));
        urlWithPlaceholders = urlWithPlaceholders.replaceAll(RaspberrypiConstants.MQTT_BROKER_HOST,
                       System.getProperty(RaspberrypiConstants.DEFAULT_CARBON_SERVER_HOST_PROPERTY, "localhost"));
        return urlWithPlaceholders;
    }

    private static int getPortOffset() {
        ServerConfiguration carbonConfig = ServerConfiguration.getInstance();
        String portOffset = System.getProperty("portOffset", carbonConfig.getFirstProperty(
                RaspberrypiConstants.CARBON_CONFIG_PORT_OFFSET));
        try {
            if ((portOffset != null)) {
                return Integer.parseInt(portOffset.trim());
            } else {
                return RaspberrypiConstants.CARBON_DEFAULT_PORT_OFFSET;
            }
        } catch (NumberFormatException e) {
            return RaspberrypiConstants.CARBON_DEFAULT_PORT_OFFSET;
        }
    }

}
