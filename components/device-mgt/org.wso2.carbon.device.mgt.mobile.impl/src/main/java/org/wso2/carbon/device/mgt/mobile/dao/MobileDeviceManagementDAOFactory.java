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

package org.wso2.carbon.device.mgt.mobile.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.mobile.common.MobileDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.mobile.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.device.mgt.mobile.config.datasource.MobileDataSourceConfig;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Factory class used to create MobileDeviceManagement related DAO objects.
 */
public abstract class MobileDeviceManagementDAOFactory implements MobileDeviceManagementDAOFactoryInterface {

    private static final Log log = LogFactory.getLog(MobileDeviceManagementDAOFactory.class);
    private static Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
    private static Map<String, Boolean> dataSourceXAEnabledMap = new HashMap<String, Boolean>();
    private static boolean isInitialized;
    private static boolean isXAEnabled;

    public static void init(Map<String, MobileDataSourceConfig> mobileDataSourceConfigMap)
            throws MobileDeviceMgtPluginException {
        DataSource dataSource;
        for (String pluginType : mobileDataSourceConfigMap.keySet()) {
            if (dataSourceMap.get(pluginType) == null) {
                MobileDataSourceConfig mobileDataSourceConfig = mobileDataSourceConfigMap.get(pluginType);
                dataSource = MobileDeviceManagementDAOFactory.resolveDataSource(mobileDataSourceConfigMap.get
                        (pluginType));
                dataSourceMap.put(pluginType, dataSource);
                dataSourceXAEnabledMap.put(pluginType,mobileDataSourceConfig.isXAEnabled());
            }
        }
        isInitialized = true;
    }

    public static void init(String key, MobileDataSourceConfig mobileDataSourceConfig) throws
            MobileDeviceMgtPluginException {
        DataSource dataSource = MobileDeviceManagementDAOFactory.resolveDataSource(mobileDataSourceConfig);
        dataSourceMap.put(key, dataSource);
    }

    /**
     * Resolve data source from the data source definition.
     *
     * @param config Mobile data source configuration
     * @return data source resolved from the data source definition
     */
    public static DataSource resolveDataSource(MobileDataSourceConfig config) {
        DataSource dataSource = null;
        if (config == null) {
            throw new RuntimeException("Device Management Repository data source configuration " +
                    "is null and thus, is not initialized");
        }
        JNDILookupDefinition jndiConfig = config.getJndiLookupDefinition();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing Device Management Repository data source using the JNDI " +
                        "Lookup Definition");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList =
                    jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<Object, Object>();
                for (JNDILookupDefinition.JNDIProperty prop : jndiPropertyList) {
                    jndiProperties.put(prop.getName(), prop.getValue());
                }
                dataSource =
                        MobileDeviceManagementDAOUtil
                                .lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource = MobileDeviceManagementDAOUtil
                        .lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }

    public static Map<String, DataSource> getDataSourceMap() {
        return dataSourceMap;
    }

    public boolean isXAEnabledDataSource(String pluginType){
        return dataSourceXAEnabledMap.get(pluginType);
    }
}