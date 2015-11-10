/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.mobile.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.mobile.common.MobileDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.mobile.config.MobileDeviceConfigurationManager;
import org.wso2.carbon.device.mgt.mobile.config.MobileDeviceManagementConfig;
import org.wso2.carbon.device.mgt.mobile.config.datasource.MobileDataSourceConfig;
import org.wso2.carbon.device.mgt.mobile.dao.AbstractMobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.mobile.impl.windows.WindowsDeviceManagementService;
import org.wso2.carbon.device.mgt.mobile.impl.windows.WindowsPolicyMonitoringService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.policy.mgt.common.spi.PolicyMonitoringService;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.Map;

/**
 * @scr.component name="org.wso2.carbon.device.mgt.mobile.impl.internal.MobileDeviceManagementServiceComponent"
 * immediate="true"
 * @scr.reference name="org.wso2.carbon.ndatasource"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="0..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * <p/>
 * Adding reference to API Manager Configuration service is an unavoidable hack to get rid of NPEs thrown while
 * initializing APIMgtDAOs attempting to register APIs programmatically. APIMgtDAO needs to be proper cleaned up
 * to avoid as an ideal fix
 */
public class MobileDeviceManagementServiceComponent {

    private ServiceRegistration windowsServiceRegRef;

    private static final Log log = LogFactory.getLog(MobileDeviceManagementServiceComponent.class);

    protected void activate(ComponentContext ctx) {

        if (log.isDebugEnabled()) {
            log.debug("Activating Mobile Device Management Service Component");
        }
        try {
            BundleContext bundleContext = ctx.getBundleContext();

            /* Initialize the data source configuration */
            MobileDeviceConfigurationManager.getInstance().initConfig();
            MobileDeviceManagementConfig config = MobileDeviceConfigurationManager.getInstance()
                    .getMobileDeviceManagementConfig();
            Map<String, MobileDataSourceConfig> dsConfigMap =
                    config.getMobileDeviceMgtRepository().getMobileDataSourceConfigMap();

            AbstractMobileDeviceManagementDAOFactory.init(dsConfigMap);

            String setupOption = System.getProperty("setup");
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "-Dsetup is enabled. Mobile Device management repository schema initialization is about " +
                                    "to begin");
                }
                try {
                    for (String pluginType : dsConfigMap.keySet()) {
                        MobileDeviceManagementDAOUtil
                                .setupMobileDeviceManagementSchema(AbstractMobileDeviceManagementDAOFactory.getDataSourceMap
                                        ().get(pluginType), pluginType);
                    }
                } catch (MobileDeviceMgtPluginException e) {
                    log.error("Exception occurred while initializing mobile device management database schema", e);
                }
            }

            windowsServiceRegRef =
                    bundleContext.registerService(DeviceManagementService.class.getName(),
                            new WindowsDeviceManagementService(), null);

            // Policy management service

            bundleContext.registerService(PolicyMonitoringService.class,
                    new WindowsPolicyMonitoringService(), null);

            if (log.isDebugEnabled()) {
                log.debug("Mobile Device Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Mobile Device Management Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Mobile Device Management Service Component");
        }
        try {
            if (windowsServiceRegRef != null) {
                windowsServiceRegRef.unregister();
            }
            if (log.isDebugEnabled()) {
                log.debug(
                        "Mobile Device Management Service Component has been successfully de-activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while de-activating Mobile Device Management bundle", e);
        }
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        /* This is to avoid mobile device management component getting initialized before the underlying datasources
        are registered */
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to mobile service component");
        }
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        //do nothing
    }

    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService acquired");
        }
        MobileDeviceManagementDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        MobileDeviceManagementDataHolder.getInstance().setRegistryService(null);
    }

}
