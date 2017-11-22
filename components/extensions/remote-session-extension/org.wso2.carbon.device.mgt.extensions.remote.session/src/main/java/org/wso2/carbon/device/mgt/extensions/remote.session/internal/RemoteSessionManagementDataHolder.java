/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.extensions.remote.session.internal;

import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.extensions.remote.session.authentication.OAuthAuthenticator;
import org.wso2.carbon.device.mgt.extensions.remote.session.dto.RemoteSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class {@link RemoteSessionManagementDataHolder} will hold the configurations and in memory storage strictures to
 * manage remote sessions.
 */
public class RemoteSessionManagementDataHolder {

    private static RemoteSessionManagementDataHolder thisInstance = new RemoteSessionManagementDataHolder();
    private DeviceManagementProviderService deviceManagementProviderService;
    private DeviceAccessAuthorizationService deviceAccessAuthorizationService;
    private boolean isEnabled;
    private String serverUrl;
    private long maxIdleTimeout;
    private int maxMessageBufferSize;
    private int maxMessagesPerSecond;
    private OAuthAuthenticator oAuthAuthenticator;
    private Map<String, RemoteSession> activeDeviceClientSessionMap = new ConcurrentHashMap<String, RemoteSession>();
    private Map<String, RemoteSession> sessionMap = new ConcurrentHashMap<String, RemoteSession>();

    public static RemoteSessionManagementDataHolder getInstance() {
        return thisInstance;
    }

    public DeviceManagementProviderService getDeviceManagementProviderService() {
        return deviceManagementProviderService;
    }

    public void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        this.deviceManagementProviderService = deviceManagementProviderService;
    }

    public DeviceAccessAuthorizationService getDeviceAccessAuthorizationService() {
        return deviceAccessAuthorizationService;
    }

    public void setDeviceAccessAuthorizationService(DeviceAccessAuthorizationService deviceAccessAuthorizationService) {
        this.deviceAccessAuthorizationService = deviceAccessAuthorizationService;
    }

    public OAuthAuthenticator getOauthAuthenticator() {
        return oAuthAuthenticator;
    }

    public void setOauthAuthenticator(OAuthAuthenticator oAuthAuthenticator) {
        this.oAuthAuthenticator = oAuthAuthenticator;
    }

    public Map<String, RemoteSession> getSessionMap() {
        return sessionMap;
    }

    public Map<String, RemoteSession> getActiveDeviceClientSessionMap() {
        return activeDeviceClientSessionMap;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public int getMaxMessagesPerSecond() {
        return maxMessagesPerSecond;
    }

    public void setMaxMessagesPerSecond(int maxMessagesPerSecond) {
        this.maxMessagesPerSecond = maxMessagesPerSecond;
    }

    public long getMaxIdleTimeout() {
        return maxIdleTimeout;
    }

    public void setMaxIdleTimeout(long maxIdleTimeout) {
        this.maxIdleTimeout = maxIdleTimeout;
    }

    public int getMaxMessageBufferSize() {
        return maxMessageBufferSize;
    }

    public void setMaxMessageBufferSize(int MaxMessageBufferSize) {
        this.maxMessageBufferSize = MaxMessageBufferSize;
    }
}
