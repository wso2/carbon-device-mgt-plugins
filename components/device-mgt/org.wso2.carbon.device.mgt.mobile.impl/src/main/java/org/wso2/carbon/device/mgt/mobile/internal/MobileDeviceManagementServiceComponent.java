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
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.spi.DeviceManager;
import org.wso2.carbon.device.mgt.common.spi.DeviceMgtService;
import org.wso2.carbon.device.mgt.mobile.common.MobileDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.mobile.config.MobileDeviceConfigurationManager;
import org.wso2.carbon.device.mgt.mobile.config.MobileDeviceManagementConfig;
import org.wso2.carbon.device.mgt.mobile.config.datasource.MobileDataSourceConfig;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.mobile.impl.android.AndroidDeviceManager;
import org.wso2.carbon.device.mgt.mobile.impl.android.dao.AndroidDAOFactory;
import org.wso2.carbon.device.mgt.mobile.impl.windows.WindowsDeviceManager;
import org.wso2.carbon.ndatasource.core.DataSourceService;

import javax.sql.DataSource;
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
 * <p/>
 * Adding reference to API Manager Configuration service is an unavoidable hack to get rid of NPEs thrown while
 * initializing APIMgtDAOs attempting to register APIs programmatically. APIMgtDAO needs to be proper cleaned up
 * to avoid as an ideal fix
 */
public class MobileDeviceManagementServiceComponent {

    private ServiceRegistration serverStartupObserverRef;
    private ServiceRegistration androidServiceRegRef;
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
            MobileDeviceManagementDAOFactory.setMobileDataSourceConfigMap(dsConfigMap);

            AndroidDAOFactory
                    .init(dsConfigMap.get(DeviceManagementConstants.MobileDeviceTypes.
                                                  MOBILE_DEVICE_TYPE_ANDROID));

            String setupOption = System.getProperty("setup");
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "-Dsetup is enabled. Mobile Device management repository schema initialization is about " +
                                    "to begin");
                }
                try {
                    Map<String, DataSource> dataSourceMap = MobileDeviceManagementDAOFactory.
                                                                               getDataSourceMap();
                    for (DataSource dataSource : dataSourceMap.values()) {
                        MobileDeviceManagementDAOUtil
                                .setupMobileDeviceManagementSchema(dataSource);
                    }
                } catch (MobileDeviceMgtPluginException e) {
                    log.error("Exception occurred while initializing mobile device management database schema", e);
                }
            }

            androidServiceRegRef =
                    bundleContext.registerService(DeviceMgtService.class.getName(), new AndroidDeviceManager(), null);
            windowsServiceRegRef =
                    bundleContext.registerService(DeviceMgtService.class.getName(), new WindowsDeviceManager(), null);

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
            if (androidServiceRegRef != null) {
                androidServiceRegRef.unregister();
            }
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
}
