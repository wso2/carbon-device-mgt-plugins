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

package org.wso2.carbon.device.mgt.iot.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.device.mgt.iot.config.server.DeviceManagementConfigurationManager;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppServerClient;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.service.ConfigurationService;
import org.wso2.carbon.device.mgt.iot.service.ConfigurationServiceImpl;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.carbon.device.mgt.iot.internal.IotDeviceManagementServiceComponent"
 * immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="0..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class IotDeviceManagementServiceComponent {

    private static final Log log = LogFactory.getLog(IotDeviceManagementServiceComponent.class);
    public static ConfigurationContextService configurationContextService;

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Iot Device Management Service Component");
        }
        try {
            BundleContext bundleContext = ctx.getBundleContext();
            /* Initialize the data source configuration */
            DeviceManagementConfigurationManager.getInstance().initConfig();
            bundleContext.registerService(ServerStartupObserver.class.getName(), new IoTServerStartupListener(), null);
            IoTCommonDataHolder.getInstance().initialize();
            bundleContext.registerService(ConfigurationService.class.getName(),
                                          new ConfigurationServiceImpl(), null);
            if (log.isDebugEnabled()) {
                log.debug("Iot Device Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Iot Device Management Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        XmppConfig xmppConfig = XmppConfig.getInstance();
        try {
            if (xmppConfig.isEnabled()) {
                XmppServerClient xmppServerClient = new XmppServerClient();
                xmppServerClient.initControlQueue();
                xmppServerClient.deleteCurrentXmppSessions();
            }
        } catch (DeviceControllerException e) {
            String errorMsg = "An error occurred whilst trying to delete all existing XMPP login sessions at " +
                    "[" + xmppConfig.getXmppEndpoint() + "].";
            log.error(errorMsg, e);
        }

        if (log.isDebugEnabled()) {
            log.debug("De-activating Iot Device Management Service Component");
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting ConfigurationContextService");
        }

        IotDeviceManagementServiceComponent.configurationContextService = configurationContextService;

    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting ConfigurationContextService");
        }
        IotDeviceManagementServiceComponent.configurationContextService = null;
    }
}
