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

import org.wso2.carbon.device.mgt.extensions.remote.session.authentication.OAuthAuthenticator;
import org.wso2.carbon.device.mgt.extensions.remote.session.constants.RemoteSessionConstants;
import org.wso2.carbon.device.mgt.extensions.remote.session.internal.RemoteSessionManagementDataHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Startup listener is been used to make sure the reciever gets activated after the server start up to avoid
 * Bundle not loading issues.
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

            RemoteSessionManagementDataHolder.getInstance().setEnabled(DeviceConfigurationManager.getInstance()
                    .getDeviceManagementConfig().getRemoteSessionConfiguration().getIsEnabled());
            RemoteSessionManagementDataHolder.getInstance().setServerUrl(DeviceConfigurationManager.getInstance()
                    .getDeviceManagementConfig().getRemoteSessionConfiguration().getRemoteSessionServerUrl());
            Map<String, String> configProperties = new HashMap<>();
            configProperties.put(RemoteSessionConstants.TOKEN_VALIDATION_ENDPOINT_URL, "https://localhost:9443");
            configProperties.put(RemoteSessionConstants.USERNAME,"admin");
            configProperties.put(RemoteSessionConstants.PASSWORD,"admin");
            configProperties.put(RemoteSessionConstants.MAXIMUM_HTTP_CONNECTION_PER_HOST,"2");
            configProperties.put(RemoteSessionConstants.MAXIMUM_TOTAL_HTTP_CONNECTION,"100");
            OAuthAuthenticator oAuthAuthenticator= new OAuthAuthenticator();
            oAuthAuthenticator.init(configProperties);
            RemoteSessionManagementDataHolder.getInstance().setOauthAuthenticator(oAuthAuthenticator);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
