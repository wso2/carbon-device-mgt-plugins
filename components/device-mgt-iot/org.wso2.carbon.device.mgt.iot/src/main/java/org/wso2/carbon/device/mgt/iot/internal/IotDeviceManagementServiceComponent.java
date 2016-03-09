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
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.device.mgt.iot.UserManagement;
import org.wso2.carbon.device.mgt.iot.analytics.statistics.IoTEventsStatisticsClient;
import org.wso2.carbon.device.mgt.iot.analytics.statistics.IoTUsageStatisticsClient;
import org.wso2.carbon.device.mgt.iot.config.devicetype.IotDeviceTypeConfigurationManager;
import org.wso2.carbon.device.mgt.iot.config.devicetype.datasource.IotDeviceTypeConfig;
import org.wso2.carbon.device.mgt.iot.config.server.DeviceManagementConfigurationManager;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppServerClient;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.service.ConfigurationService;
import org.wso2.carbon.device.mgt.iot.service.ConfigurationServiceImpl;
import org.wso2.carbon.device.mgt.iot.service.DeviceTypeService;
import org.wso2.carbon.device.mgt.iot.service.DeviceTypeServiceImpl;
import org.wso2.carbon.device.mgt.iot.service.StartupListener;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dao.IotDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dao.util.IotDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.exception.IotDeviceMgtPluginException;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.Map;

/**
 * @scr.component name="org.wso2.carbon.device.mgt.iot.internal.IotDeviceManagementServiceComponent"
 * immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.ndatasource"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="0..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="databridge.component"
 * interface="org.wso2.carbon.databridge.core.DataBridgeReceiverService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataBridgeReceiverService"
 * unbind="unsetDataBridgeReceiverService"
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
            IotDeviceTypeConfigurationManager.getInstance().initConfig();
            Map<String, IotDeviceTypeConfig> dsConfigMap =
                    IotDeviceTypeConfigurationManager.getInstance().getIotDeviceTypeConfigMap();
            IotDeviceManagementDAOFactory.init(dsConfigMap);

            bundleContext.registerService(ServerStartupObserver.class.getName(), new StartupListener(), null);

            String setupOption = System.getProperty("setup");
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "-Dsetup is enabled. Iot Device management repository schema initialization is about " +
                            "to begin");
                }
                try {
                    for (String pluginType : dsConfigMap.keySet()) {
                        IotDeviceManagementDAOUtil
                                .setupIotDeviceManagementSchema(
                                        IotDeviceManagementDAOFactory.getDataSourceMap
                                                ().get(pluginType), pluginType);
                    }
                } catch (IotDeviceMgtPluginException e) {
                    log.error(
                            "Exception occurred while initializing mobile device management database schem ",
                            e);
                }
            }

            IoTCommonDataHolder.getInstance().initialize();

            //TODO: handle
            IoTUsageStatisticsClient.initializeDataSource();
            IoTEventsStatisticsClient.initializeDataSource();
            UserManagement.registerApiAccessRoles();
            bundleContext.registerService(DeviceTypeService.class.getName(), new DeviceTypeServiceImpl(), null);
            bundleContext.registerService(ConfigurationService.class.getName(), new ConfigurationServiceImpl(), null);

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

    protected void setDataSourceService(DataSourceService dataSourceService) {
        /* This is to avoid iot device management component getting initialized before the
        underlying datasources
        are registered */
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to mobile service component");
        }
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        //do nothing
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

    /**
     * Sets Realm Service
     *
     * @param realmService associated realm service reference
     */
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");

        }
        UserManagement.setRealmService(realmService);
    }

    /**
     * Unsets Realm Service
     *
     * @param realmService associated realm service reference
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        UserManagement.setRealmService(realmService);
    }

    /**
     * Sets DataBridge Receiver Service
     *
     * @param dataBridgeReceiverService associated DataBridge service reference
     */
    protected void setDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting DataBridge Receiver Service");
        }
        IoTCommonDataHolder.getInstance().setDataBridgeReceiverService(dataBridgeReceiverService);
    }

    /**
     * Unsets Realm Service
     *
     * @param dataBridgeReceiverService associated DataBridge service reference
     */
    protected void unsetDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting DataBridge Receiver Service");
        }
        IoTCommonDataHolder.getInstance().setDataBridgeReceiverService(null);
    }

}
