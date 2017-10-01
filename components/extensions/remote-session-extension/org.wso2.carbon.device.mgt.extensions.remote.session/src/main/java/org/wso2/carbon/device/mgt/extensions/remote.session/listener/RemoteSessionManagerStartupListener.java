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
package org.wso2.carbon.device.mgt.extensions.remote.session.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.keymanager.KeyManagerConfigurations;
import org.wso2.carbon.device.mgt.core.config.remote.session.RemoteSessionConfiguration;
import org.wso2.carbon.device.mgt.extensions.remote.session.authentication.OAuthAuthenticator;
import org.wso2.carbon.device.mgt.extensions.remote.session.constants.RemoteSessionConstants;
import org.wso2.carbon.device.mgt.extensions.remote.session.internal.RemoteSessionManagementDataHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Startup listener is been used to make sure the receiver gets activated after the server start up to avoid
 * Bundle not loading issues.
 * This will configure the values for remote session management
 */
public class RemoteSessionManagerStartupListener implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(RemoteSessionManagerStartupListener.class);

    @Override
    public void completingServerStartup() {
    }

    @Override
    public void completedServerStartup() {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        try {
            RemoteSessionConfiguration rsConfig = DeviceConfigurationManager.getInstance().getDeviceManagementConfig
                    ().getRemoteSessionConfiguration();
            KeyManagerConfigurations kmConfig = DeviceConfigurationManager.getInstance().getDeviceManagementConfig()
                    .getKeyManagerConfigurations();

            RemoteSessionManagementDataHolder.getInstance().setEnabled(rsConfig.isEnabled());
            RemoteSessionManagementDataHolder.getInstance().setServerUrl(rsConfig.getRemoteSessionServerUrl());
            Map<String, String> configProperties = new HashMap<>();

            // Set max idle timeout in milliseconds
            RemoteSessionManagementDataHolder.getInstance().setMaxIdleTimeout((long) rsConfig.getSessionIdleTimeOut() *
                    60000);

            // Set max messages per second.
            if (rsConfig.getMaxMessagesPerSession() > 0) {
                RemoteSessionManagementDataHolder.getInstance().setMaxMessagesPerSecond(rsConfig
                        .getMaxMessagesPerSession());
            }
            // Token validation related configuration
            configProperties.put(RemoteSessionConstants.TOKEN_VALIDATION_ENDPOINT_URL, kmConfig.getServerUrl());
            configProperties.put(RemoteSessionConstants.USERNAME, kmConfig.getAdminUsername());
            configProperties.put(RemoteSessionConstants.PASSWORD, kmConfig.getAdminPassword());
            if (rsConfig.getMaxHTTPConnectionPerHost() > 0) {

                configProperties.put(RemoteSessionConstants.MAXIMUM_HTTP_CONNECTION_PER_HOST,
                        String.valueOf(rsConfig.getMaxHTTPConnectionPerHost()));
            } else {
                configProperties.put(RemoteSessionConstants.MAXIMUM_HTTP_CONNECTION_PER_HOST, RemoteSessionConstants
                        .DEFAULT_MAXIMUM_HTTP_CONNECTION_PER_HOST);
            }
            if (rsConfig.getMaxTotalHTTPConnections() > 0) {
                configProperties.put(RemoteSessionConstants.MAXIMUM_TOTAL_HTTP_CONNECTION, String.valueOf(rsConfig
                        .getMaxTotalHTTPConnections()));
            } else {
                configProperties.put(RemoteSessionConstants.MAXIMUM_TOTAL_HTTP_CONNECTION, RemoteSessionConstants
                        .DEFAULT_MAXIMUM_TOTAL_HTTP_CONNECTIONS);
            }

            OAuthAuthenticator oAuthAuthenticator = new OAuthAuthenticator();
            oAuthAuthenticator.init(configProperties);
            RemoteSessionManagementDataHolder.getInstance().setOauthAuthenticator(oAuthAuthenticator);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
