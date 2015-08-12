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

package org.wso2.carbon.device.mgt.mobile.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.mobile.common.MobileDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.mobile.common.MobilePluginConstants;
import org.wso2.carbon.device.mgt.mobile.dto.*;
import org.wso2.carbon.device.mgt.mobile.internal.MobileDeviceManagementDataHolder;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

/**
 * Provides utility methods required by the mobile device management bundle.
 */
public class MobileDeviceManagementUtil {

	private static final Log log = LogFactory.getLog(MobileDeviceManagementUtil.class);
	private static final String MOBILE_DEVICE_IMEI = "IMEI";
	private static final String MOBILE_DEVICE_IMSI = "IMSI";
	private static final String MOBILE_DEVICE_VENDOR = "VENDOR";
	private static final String MOBILE_DEVICE_OS_VERSION = "OS_VERSION";
	private static final String MOBILE_DEVICE_MODEL = "DEVICE_MODEL";
	private static final String MOBILE_DEVICE_LATITUDE = "LATITUDE";
	private static final String MOBILE_DEVICE_LONGITUDE = "LONGITUDE";
	private static final String MOBILE_DEVICE_SERIAL = "SERIAL";

	public static Document convertToDocument(File file) throws DeviceManagementException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			return docBuilder.parse(file);
		} catch (Exception e) {
			throw new DeviceManagementException(
					"Error occurred while parsing file, while converting " +
					"to a org.w3c.dom.Document : " + e.getMessage(), e);
		}
	}

	private static String getPropertyValue(Device device, String property) {
		for (Device.Property prop : device.getProperties()) {
			if (property.equals(prop.getName())) {
				return prop.getValue();
			}
		}
		return null;
	}

	private static Device.Property getProperty(String property, String value) {
		if (property != null) {
			Device.Property prop = new Device.Property();
			prop.setName(property);
			prop.setValue(value);
			return prop;
		}
		return null;
	}

	public static MobileDevice convertToMobileDevice(Device device) {
		MobileDevice mobileDevice = null;
		if (device != null) {
			mobileDevice = new MobileDevice();
			mobileDevice.setMobileDeviceId(device.getDeviceIdentifier());
			mobileDevice.setImei(getPropertyValue(device, MOBILE_DEVICE_IMEI));
			mobileDevice.setImsi(getPropertyValue(device, MOBILE_DEVICE_IMSI));
			mobileDevice.setModel(getPropertyValue(device, MOBILE_DEVICE_MODEL));
			mobileDevice.setOsVersion(getPropertyValue(device, MOBILE_DEVICE_OS_VERSION));
			mobileDevice.setVendor(getPropertyValue(device, MOBILE_DEVICE_VENDOR));
			mobileDevice.setLatitude(getPropertyValue(device, MOBILE_DEVICE_LATITUDE));
			mobileDevice.setLongitude(getPropertyValue(device, MOBILE_DEVICE_LONGITUDE));

			if (device.getProperties() != null) {
				Map<String, String> deviceProperties = new HashMap<String, String>();
				for (Device.Property deviceProperty : device.getProperties()) {
					deviceProperties.put(deviceProperty.getName(), deviceProperty.getValue());
				}

				mobileDevice.setDeviceProperties(deviceProperties);
			} else {
				mobileDevice.setDeviceProperties(new HashMap<String, String>());
			}
		}
		return mobileDevice;
	}

	public static Device convertToDevice(MobileDevice mobileDevice) {
		Device device = null;
		if (mobileDevice != null) {
			device = new Device();
			List<Device.Property> propertyList = new ArrayList<Device.Property>();
			propertyList.add(getProperty(MOBILE_DEVICE_IMEI, mobileDevice.getImei()));
			propertyList.add(getProperty(MOBILE_DEVICE_IMSI, mobileDevice.getImsi()));
			propertyList.add(getProperty(MOBILE_DEVICE_MODEL, mobileDevice.getModel()));
			propertyList.add(getProperty(MOBILE_DEVICE_OS_VERSION, mobileDevice.getOsVersion()));
			propertyList.add(getProperty(MOBILE_DEVICE_VENDOR, mobileDevice.getVendor()));
			propertyList.add(getProperty(MOBILE_DEVICE_LATITUDE, mobileDevice.getLatitude()));
			propertyList.add(getProperty(MOBILE_DEVICE_LONGITUDE, mobileDevice.getLongitude()));
			propertyList.add(getProperty(MOBILE_DEVICE_SERIAL, mobileDevice.getSerial()));

			if (mobileDevice.getDeviceProperties() != null) {
				for (Map.Entry<String, String> deviceProperty : mobileDevice.getDeviceProperties()
				                                                            .entrySet()) {
					propertyList
							.add(getProperty(deviceProperty.getKey(), deviceProperty.getValue()));
				}
			}

			device.setProperties(propertyList);
			device.setDeviceIdentifier(mobileDevice.getMobileDeviceId());
		}
		return device;
	}

	public static MobileOperation convertToMobileOperation(Operation operation) {
		MobileOperation mobileOperation = new MobileOperation();
		MobileOperationProperty operationProperty;
		List<MobileOperationProperty> properties = new LinkedList<MobileOperationProperty>();
		mobileOperation.setFeatureCode(operation.getCode());
		mobileOperation.setCreatedDate(new Date().getTime());
		Properties operationProperties = operation.getProperties();
		for (String key : operationProperties.stringPropertyNames()) {
			operationProperty = new MobileOperationProperty();
			operationProperty.setProperty(key);
			operationProperty.setValue(operationProperties.getProperty(key));
			properties.add(operationProperty);
		}
		mobileOperation.setProperties(properties);
		return mobileOperation;
	}

	public static List<Integer> getMobileOperationIdsFromMobileDeviceOperations(
			List<MobileDeviceOperationMapping> mobileDeviceOperationMappings) {
		List<Integer> mobileOperationIds = new ArrayList<Integer>();
		for (MobileDeviceOperationMapping mobileDeviceOperationMapping : mobileDeviceOperationMappings) {
			mobileOperationIds.add(mobileDeviceOperationMapping.getOperationId());
		}
		return mobileOperationIds;
	}

	public static Operation convertMobileOperationToOperation(MobileOperation mobileOperation) {
		Operation operation = new Operation();
		Properties properties = new Properties();
		operation.setCode(mobileOperation.getFeatureCode());
		for (MobileOperationProperty mobileOperationProperty : mobileOperation.getProperties()) {
			properties
					.put(mobileOperationProperty.getProperty(), mobileOperationProperty.getValue());
		}
		operation.setProperties(properties);
		return operation;
	}

	public static MobileFeature convertToMobileFeature(Feature feature) {
		MobileFeature mobileFeature = new MobileFeature();
		mobileFeature.setName(feature.getName());
		mobileFeature.setCode(feature.getCode());
		mobileFeature.setDescription(feature.getDescription());
		mobileFeature.setDeviceType(feature.getDeviceType());
		return mobileFeature;
	}

	public static Feature convertToFeature(MobileFeature mobileFeature) {
		Feature feature = new Feature();
		feature.setDescription(mobileFeature.getDescription());
		feature.setDeviceType(mobileFeature.getDeviceType());
		feature.setCode(mobileFeature.getCode());
		feature.setName(mobileFeature.getName());
		return feature;
	}

	public static Registry getRegistry() throws MobileDeviceMgtPluginException {
		try {
			int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
			return MobileDeviceManagementDataHolder.getInstance().getRegistryService()
			                                              .getConfigSystemRegistry(
					                                              tenantId);
		} catch (RegistryException e) {
			throw new MobileDeviceMgtPluginException(
					"Error in retrieving conf registry instance: " +
					e.getMessage(), e);
		}
	}

	public static Resource getRegistryResource(String path) throws MobileDeviceMgtPluginException {
		try {
			return MobileDeviceManagementUtil.getRegistry().get(path);

		} catch (RegistryException e) {
			throw new MobileDeviceMgtPluginException("Error in retrieving registry resource : " +
			                                         e.getMessage(), e);
		}
	}

	public static boolean putRegistryResource(String path,
	                                          Resource resource)
			throws MobileDeviceMgtPluginException {
		boolean status = false;
		try {
			MobileDeviceManagementUtil.getRegistry().beginTransaction();
			MobileDeviceManagementUtil.getRegistry().put(path, resource);
			MobileDeviceManagementUtil.getRegistry().commitTransaction();
			status = true;
		} catch (RegistryException e) {
			throw new MobileDeviceMgtPluginException(
					"Error occurred while persisting registry resource : " +
					e.getMessage(), e);
		}
		return status;
	}

	public static String getResourcePath(String resourceName, String platform) {
		String regPath = "";
		switch (platform) {
			case DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID:
				regPath = MobilePluginConstants.MOBILE_CONFIG_REGISTRY_ROOT + "/" +
				          DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID +
				          "/" + resourceName;
				break;
			case DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS:
				regPath = MobilePluginConstants.MOBILE_CONFIG_REGISTRY_ROOT + "/" +
				          DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS +
				          "/" + resourceName;
				break;
			case DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS:
				regPath = MobilePluginConstants.MOBILE_CONFIG_REGISTRY_ROOT + "/" +
						DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS +
						"/" + resourceName;
				break;
		}
		return regPath;
	}

	public static String getPlatformConfigPath(String platform) {
		String regPath = "";
		switch (platform) {
			case DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID:
				regPath = DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID;
				break;
			case DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS:
				regPath = DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS;
				break;
			case DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS:
				regPath = DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS;
				break;
		}
		return regPath;
	}

	public static boolean createRegistryCollection(String path)
			throws MobileDeviceMgtPluginException {
		try {
			if (! MobileDeviceManagementUtil.getRegistry().resourceExists(path)) {
				Resource resource = MobileDeviceManagementUtil.getRegistry().newCollection();
				MobileDeviceManagementUtil.getRegistry().beginTransaction();
				MobileDeviceManagementUtil.getRegistry().put(path, resource);
				MobileDeviceManagementUtil.getRegistry().commitTransaction();
			}
			return true;
		} catch (MobileDeviceMgtPluginException e) {
			throw new MobileDeviceMgtPluginException(
					"Error occurred while creating a registry collection : " +
					e.getMessage(), e);
		} catch (RegistryException e) {
			throw new MobileDeviceMgtPluginException(
					"Error occurred while creating a registry collection : " +
					e.getMessage(), e);
		}
	}
}
