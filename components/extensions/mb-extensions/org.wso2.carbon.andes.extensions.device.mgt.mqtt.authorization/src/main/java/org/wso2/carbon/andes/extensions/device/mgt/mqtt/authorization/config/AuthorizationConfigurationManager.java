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

import java.util.ArrayList;
import java.util.List;

public class AuthorizationConfigurationManager {

    private static final String CONNECTION_PERMISSION = "connectionPermission";
    private static final String MQTT_PUBLISHER_PERMISSION = "publisherPermission";
    private static final String MQTT_SUBSCRIBER_PERMISSION = "subscriberPermission";
    private static final String CONNECTION_USERNAME = "username";
    private static final String CONNECTION_PASSWORD = "password";
    private static final String TOKEN_ENDPOINT = "tokenEndpoint";
    private static final String TOKEN_REFRESH_TIME_OFFSET = "tokenRefreshTimeOffset";
    private static final String DEVICE_MGT_SERVER_URL = "deviceMgtServerUrl";
    private static final String MQTT_CACHE_DURATION = "cacheDurationSeconds";

    private static final AuthorizationConfigurationManager oAuthConfigurationManager
            = new AuthorizationConfigurationManager();
    private static Log logger = LogFactory.getLog(AuthorizationConfigurationManager.class);
    private String connectionPermission;
    private String username;
    private String password;
    private String tokenEndpoint;
    private long tokenRefreshTimeOffset;
    private String deviceMgtServerUrl;
    private long cacheDuration;

    private List<String> publisherPermissions = new ArrayList<>();
    private List<String> subscriberPermissions = new ArrayList<>();

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

    public List<String> getPublisherPermissions() {
        return publisherPermissions;
    }

    public void setPublisherPermission(String publisherPermission) {
        if (publisherPermission != null && !publisherPermission.isEmpty()) {
            this.publisherPermissions.add(publisherPermission);
        } else {
            logger.error("MQTT publisher permission can't be empty ");
        }
    }

    public List<String> getSubscriberPermissions() {
        return subscriberPermissions;
    }

    public void setSubscriberPermission(String subscriberPermission) {
        if (subscriberPermission != null && !subscriberPermission.isEmpty()) {
            this.subscriberPermissions.add(subscriberPermission);
        } else {
            logger.error("MQTT subscriber permissions can't be null ");
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username != null && !username.isEmpty()) {
            this.username = username;
        } else {
            logger.error("username can't be empty ");
        }

    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null && !password.isEmpty()) {
            this.password = password;
        } else {
            logger.error("password can't be empty ");
        }
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        if (tokenEndpoint != null && !tokenEndpoint.isEmpty()) {
            this.tokenEndpoint = tokenEndpoint;
        } else {
            logger.error("tokenEndpoint can't be empty ");
        }
    }

    public long getTokenRefreshTimeOffset() {
        return tokenRefreshTimeOffset;
    }

    public void setTokenRefreshTimeOffset(String tokenRefreshTimeOffset) {
        try {
            if (tokenRefreshTimeOffset != null && !tokenRefreshTimeOffset.isEmpty()) {
                this.tokenRefreshTimeOffset = Long.parseLong(tokenRefreshTimeOffset);
            }
        } catch (NumberFormatException e) {
            logger.error("tokenRefreshTimeOffset is not a number(long)");
        }
    }

    public String getDeviceMgtServerUrl() {
        return deviceMgtServerUrl;
    }

    public void setDeviceMgtServerUrl(String deviceMgtServerUrl) {
        if (deviceMgtServerUrl != null && !deviceMgtServerUrl.isEmpty()) {
            this.deviceMgtServerUrl = deviceMgtServerUrl;
        } else {
            logger.error("deviceMgtServerUrl can't be empty ");
        }
    }

    public long getCacheDuration() {
        return cacheDuration;
    }

    public void setCacheDuration(String cacheDuration) {
        try {
            if (cacheDuration != null && !cacheDuration.isEmpty()) {
                this.cacheDuration = Long.parseLong(cacheDuration);
            }
        } catch (NumberFormatException e) {
            this.cacheDuration = 0;
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
                case MQTT_PUBLISHER_PERMISSION:
                    setPublisherPermission(propertyValue);
                    break;
                case MQTT_SUBSCRIBER_PERMISSION:
                    setSubscriberPermission(propertyValue);
                    break;
                case CONNECTION_USERNAME:
                    setUsername(propertyValue);
                    break;
                case CONNECTION_PASSWORD:
                    setPassword(propertyValue);
                    break;
                case TOKEN_ENDPOINT:
                    setTokenEndpoint(propertyValue);
                    break;
                case TOKEN_REFRESH_TIME_OFFSET:
                    setTokenRefreshTimeOffset(propertyValue);
                    break;
                case DEVICE_MGT_SERVER_URL:
                    setDeviceMgtServerUrl(propertyValue);
                    break;
                case MQTT_CACHE_DURATION:
                    setCacheDuration(propertyValue);
                    break;


                default:
                    break;
            }
        }
    }
}
