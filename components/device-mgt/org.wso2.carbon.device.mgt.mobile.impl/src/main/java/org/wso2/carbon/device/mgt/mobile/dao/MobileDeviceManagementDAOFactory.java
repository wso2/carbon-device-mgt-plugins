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
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.mobile.config.datasource.JNDILookupDefinition;
import org.wso2.carbon.device.mgt.mobile.config.datasource.MobileDataSourceConfig;
import org.wso2.carbon.device.mgt.mobile.dao.impl.*;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;

import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Factory class used to create MobileDeviceManagement related DAO objects.
 */
public class MobileDeviceManagementDAOFactory {

	private static final Log log = LogFactory.getLog(MobileDeviceManagementDAOFactory.class);
    private static Map<String,MobileDataSourceConfig> mobileDataSourceConfigMap;
    private static Map<String,DataSource> dataSourceMap;
    private String pluginProvider;
    private DataSource dataSource;

	public MobileDeviceManagementDAOFactory(String pluginProvider) {
        this.pluginProvider = pluginProvider;
        this.dataSource = dataSourceMap.get(pluginProvider);
	}

	public static void init() {
		try {
            DataSource dataSource;
            for(String pluginType:mobileDataSourceConfigMap.keySet()){
                dataSource =  MobileDeviceManagementDAOFactory.resolveDataSource(mobileDataSourceConfigMap.get
                        (pluginType));
                dataSourceMap.put(pluginType,dataSource);
            }
		} catch (DeviceManagementException e) {
			log.error("Exception occurred while initializing the mobile data source.",e);
		}
	}

	/**
	 * Resolve data source from the data source definition.
	 *
	 * @param config Mobile data source configuration
	 * @return data source resolved from the data source definition
	 */
	private static DataSource resolveDataSource(MobileDataSourceConfig config)
			throws DeviceManagementException {
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

	public MobileDeviceDAO getMobileDeviceDAO() {
		return new MobileDeviceDAOImpl(dataSource);
	}

	public MobileOperationDAO getMobileOperationDAO() {
		return new MobileOperationDAOImpl(dataSource);
	}

	public MobileOperationPropertyDAO getMobileOperationPropertyDAO() {
		return new MobileOperationPropertyDAOImpl(dataSource);
	}

	public MobileDeviceOperationMappingDAO getMobileDeviceOperationDAO() {
		return new MobileDeviceOperationMappingDAOImpl(dataSource);
	}

	public MobileFeatureDAO getFeatureDAO() {
		return new MobileFeatureDAOImpl(dataSource);
	}

	public MobileFeaturePropertyDAO getFeaturePropertyDAO() {
		return new MobileFeaturePropertyDAOImpl(dataSource);
	}

	public MobileDataSourceConfig getMobileDeviceManagementConfig(String pluginType) {
		return mobileDataSourceConfigMap.get(pluginType);
	}

    public static Map<String, MobileDataSourceConfig> getMobileDataSourceConfigMap() {
        return mobileDataSourceConfigMap;
    }

    public static void setMobileDataSourceConfigMap(Map<String, MobileDataSourceConfig> mobileDataSourceConfigMap) {
        MobileDeviceManagementDAOFactory.mobileDataSourceConfigMap = mobileDataSourceConfigMap;
    }

	public DataSource getDataSource(String type) {
		return dataSourceMap.get(type);
	}

    public static Map<String, DataSource> getDataSourceMap() {
        return dataSourceMap;
    }
}