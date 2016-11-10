/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class AuthorizationConfigurationManager {

    private static final String CONNECTION_PERMISSION = "connectionPermission";
    private static final String ADMIN_PERMISSION = "adminPermission";
    private static final String MQTT_PUBLISHER_SCOPE_IDENTIFIER = "MQTTPublisherScopeIdentifier";
    private static final String MQTT_SUBSCRIBER_SCOPE_IDENTIFIER = "MQTTSubscriberScopeIdentifier";
    private static final String DEVICE_MGT_SCOPE_IDENTIFIER = "devicemgtScopeIdentifier";
    private static final AuthorizationConfigurationManager oAuthConfigurationManager
            = new AuthorizationConfigurationManager();
    private static Log logger = LogFactory.getLog(AuthorizationConfigurationManager.class);
    private String connectionPermission;
    private String adminPermission;
    private String MQTTPublisherScopeIdentifier;
    private String MQTTSubscriberScopeIdentifier;
    private String devicemgtScopeIdentifier;

    private AuthorizationConfigurationManager() {

    }

    public static AuthorizationConfigurationManager getInstance() {
        return oAuthConfigurationManager;
    }

    public String getConnectionPermission() {
        return connectionPermission;
    }

    public void setConnectionPermission(String connectionPermission) {
        if (connectionPermission != null) {
            this.connectionPermission = connectionPermission;
        } else {
            logger.error("Connection permission can't be null ");
        }
    }

    public String getAdminPermission() {
        return adminPermission;
    }

    public void setAdminPermission(String adminPermission) {
        if (adminPermission != null) {
            this.adminPermission = adminPermission;
        } else {
            logger.error("admin permission can't be null ");
        }
    }

    public String getMQTTPublisherScopeIdentifier() {
        return MQTTPublisherScopeIdentifier;
    }

    public void setMQTTPublisherScopeIdentifier(String MQTTPublisherScopeIdentifier) {
        if (MQTTPublisherScopeIdentifier != null) {
            this.MQTTPublisherScopeIdentifier = MQTTPublisherScopeIdentifier;
        } else {
            logger.error("MQTT publisher scope identifier can't be null ");
        }
    }

    public String getMQTTSubscriberScopeIdentifier() {
        return MQTTSubscriberScopeIdentifier;
    }

    public void setMQTTSubscriberScopeIdentifier(String MQTTSubscriberScopeIdentifier) {
        if (MQTTSubscriberScopeIdentifier != null) {
            this.MQTTSubscriberScopeIdentifier = MQTTSubscriberScopeIdentifier;
        } else {
            logger.error("MQTT subscriber scope identifier can't be null ");
        }
    }

    public String getDevicemgtScopeIdentifier() {
        return devicemgtScopeIdentifier;
    }

    public void setDevicemgtScopeIdentifier(String devicemgtScopeIdentifier) {
        if (devicemgtScopeIdentifier != null) {
            this.devicemgtScopeIdentifier = devicemgtScopeIdentifier;
        } else {
            logger.error("Device management scope identifier can't be null ");
        }
    }

    /**
     * Initialize the configuration properties that required for MQTT Authorization
     */
    public synchronized void initConfig() {
        List<String> mqttTransportAuthorizationProperties = AuthorizationConfiguration.readValueList(MQTTConfiguration
                .LIST_TRANSPORT_MQTT_AUTHORIZATION_PROPERTIES);
        for (String property : mqttTransportAuthorizationProperties) {
            String propertyValue = AuthorizationConfiguration.readValueOfChildByKey(
                    MQTTConfiguration.TRANSPORT_MQTT_AUTHORIZATION_PROPERTIES, property);
            switch (property) {
                case CONNECTION_PERMISSION:
                    setConnectionPermission(propertyValue);
                    break;
                case ADMIN_PERMISSION:
                    setAdminPermission(propertyValue);
                    break;
                case MQTT_PUBLISHER_SCOPE_IDENTIFIER:
                    setMQTTPublisherScopeIdentifier(propertyValue);
                    break;
                case MQTT_SUBSCRIBER_SCOPE_IDENTIFIER:
                    setMQTTSubscriberScopeIdentifier(propertyValue);
                    break;
                case DEVICE_MGT_SCOPE_IDENTIFIER:
                    setDevicemgtScopeIdentifier(propertyValue);
                    break;
                default:
                    break;
            }
        }
    }
}
