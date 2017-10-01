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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.extensions.remote.session.RemoteSessionManagementService;
import org.wso2.carbon.device.mgt.extensions.remote.session.RemoteSessionManagementServiceImpl;
import org.wso2.carbon.device.mgt.extensions.remote.session.listener.RemoteSessionManagerStartupListener;

/**
 * @scr.component name="RemoteSessionManagementServiceComponent" immediate="true"
 * @scr.reference name="carbon.device.mgt.provider"
 * interface="org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceManagementProviderService"
 * unbind="unsetDeviceManagementProviderService"
 * @scr.reference name="device.manager.service"
 * interface="org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService"
 * cardinality="0..n"
 * policy="dynamic"
 * bind="setDeviceAccessAuthorizationService"
 * unbind="unsetDeviceAccessAuthorizationService"
 */

public class RemoteSessionManagementServiceComponent {

    private static final Log log = LogFactory.getLog(RemoteSessionManagementServiceComponent.class);

    @SuppressWarnings("unused")
    protected void activate(ComponentContext componentContext) {
        try {

            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(ServerStartupObserver.class.getName(), new
                    RemoteSessionManagerStartupListener(), null);
            bundleContext.registerService(RemoteSessionManagementService.class.getName(), new
                    RemoteSessionManagementServiceImpl(), null);
            if (log.isDebugEnabled()) {
                log.debug("Remote Session device access service implementation bundle has been successfully " +
                        "initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing Remote Session device access service " +
                    "implementation bundle", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //Do nothing
    }

    protected void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        RemoteSessionManagementDataHolder.getInstance()
                .setDeviceManagementProviderService(deviceManagementProviderService);
    }

    protected void unsetDeviceManagementProviderService(DeviceManagementProviderService
                                                                deviceManagementProviderService) {
        RemoteSessionManagementDataHolder.getInstance().setDeviceManagementProviderService(null);
    }

    protected void setDeviceAccessAuthorizationService(DeviceAccessAuthorizationService
                                                               deviceAccessAuthorizationService) {
        RemoteSessionManagementDataHolder.getInstance()
                .setDeviceAccessAuthorizationService(deviceAccessAuthorizationService);
    }

    protected void unsetDeviceAccessAuthorizationService(DeviceAccessAuthorizationService
                                                                 deviceAccessAuthorizationService) {
        RemoteSessionManagementDataHolder.getInstance().setDeviceManagementProviderService(null);
    }

}
