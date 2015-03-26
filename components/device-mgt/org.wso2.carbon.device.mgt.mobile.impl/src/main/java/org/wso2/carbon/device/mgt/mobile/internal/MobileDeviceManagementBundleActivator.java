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
import org.osgi.framework.*;
import org.wso2.carbon.device.mgt.common.spi.DeviceManager;
import org.wso2.carbon.device.mgt.mobile.DataSourceListener;
import org.wso2.carbon.device.mgt.mobile.config.MobileDeviceConfigurationManager;
import org.wso2.carbon.device.mgt.mobile.config.MobileDeviceManagementConfig;
import org.wso2.carbon.device.mgt.mobile.config.datasource.MobileDataSourceConfig;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.impl.android.AndroidDeviceManager;
import org.wso2.carbon.device.mgt.mobile.impl.ios.IOSDeviceManager;
import org.wso2.carbon.device.mgt.mobile.impl.windows.WindowsDeviceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BundleActivator of MobileDeviceManagement component.
 */
public class MobileDeviceManagementBundleActivator implements BundleActivator, BundleListener {

	private ServiceRegistration androidServiceRegRef;
	private ServiceRegistration iOSServiceRegRef;
	private ServiceRegistration windowsServiceRegRef;

	private static List<DataSourceListener> dataSourceListeners =
			new ArrayList<DataSourceListener>();

	private static final String SYMBOLIC_NAME_DATA_SOURCE_COMPONENT =
			"org.wso2.carbon.ndatasource.core";
	private static final Log log = LogFactory.getLog(MobileDeviceManagementBundleActivator.class);

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Activating Mobile Device Management Service bundle");
			}
			bundleContext.addBundleListener(this);

            /* Initialize the data source configuration */
            MobileDeviceConfigurationManager.getInstance().initConfig();
            MobileDeviceManagementConfig config = MobileDeviceConfigurationManager.getInstance()
                    .getMobileDeviceManagementConfig();
            Map<String, MobileDataSourceConfig> mobileDataSourceConfigMap =
                    config.getMobileDeviceMgtRepository().getMobileDataSourceConfigMap();
            MobileDeviceManagementDAOFactory.setMobileDataSourceConfigMap(mobileDataSourceConfigMap);

            androidServiceRegRef =
                    bundleContext.registerService(DeviceManager.class.getName(),
                            new AndroidDeviceManager(), null);
            iOSServiceRegRef =
                    bundleContext.registerService(DeviceManager.class.getName(),
                            new IOSDeviceManager(), null);
            windowsServiceRegRef =
                    bundleContext.registerService(DeviceManager.class.getName(),
                            new WindowsDeviceManager(), null);

			if (log.isDebugEnabled()) {
				log.debug("Mobile Device Management Service bundle is activated");
			}
		} catch (Throwable e) {
			log.error("Error occurred while activating Mobile Device Management bundle", e);
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Deactivating Mobile Device Management Service");
		}
		try {
			androidServiceRegRef.unregister();
			iOSServiceRegRef.unregister();
			windowsServiceRegRef.unregister();

			bundleContext.removeBundleListener(this);
		} catch (Throwable e) {
			log.error("Error occurred while de-activating Mobile Device Management bundle", e);
		}
	}

	@Override
	public void bundleChanged(BundleEvent bundleEvent) {
		int eventType = bundleEvent.getType();
		String bundleSymbolicName = bundleEvent.getBundle().getSymbolicName();

		if (SYMBOLIC_NAME_DATA_SOURCE_COMPONENT.equals(bundleSymbolicName) &&
		    eventType == BundleEvent.STARTED) {
			for (DataSourceListener listener : this.getDataSourceListeners()) {
				listener.notifyObserver();
			}
		}
	}

	public static void registerDataSourceListener(DataSourceListener listener) {
		dataSourceListeners.add(listener);
	}

	private List<DataSourceListener> getDataSourceListeners() {
		return dataSourceListeners;
	}


}
