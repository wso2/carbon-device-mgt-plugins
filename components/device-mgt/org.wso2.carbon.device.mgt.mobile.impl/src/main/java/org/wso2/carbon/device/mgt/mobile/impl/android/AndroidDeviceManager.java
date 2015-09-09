/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.mobile.impl.android;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.extensions.license.mgt.registry.RegistryBasedLicenseManager;
import org.wso2.carbon.device.mgt.mobile.common.MobileDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.mobile.common.MobilePluginConstants;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;
import org.wso2.carbon.device.mgt.mobile.impl.android.dao.AndroidDAOFactory;
import org.wso2.carbon.device.mgt.mobile.impl.android.util.AndroidPluginUtils;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class AndroidDeviceManager implements DeviceManager {

	private MobileDeviceManagementDAOFactory daoFactory;
	private static final Log log = LogFactory.getLog(AndroidDeviceManagementService.class);
	private FeatureManager featureManager = new AndroidFeatureManager();
	private LicenseManager licenseManager;

	public AndroidDeviceManager() {
		this.daoFactory = new AndroidDAOFactory();
		this.licenseManager = new RegistryBasedLicenseManager();

		License defaultLicense = AndroidPluginUtils.getDefaultLicense();
		try {
			licenseManager
					.addLicense(AndroidDeviceManagementService.DEVICE_TYPE_ANDROID, defaultLicense);
		} catch (LicenseManagementException e) {
			log.error("Error occurred while adding default license for Android devices", e);
		}
	}

	@Override
	public FeatureManager getFeatureManager() {
		return featureManager;
	}

	@Override
	public boolean saveConfiguration(TenantConfiguration tenantConfiguration)
			throws DeviceManagementException {
		boolean status;
		try {
			if (log.isDebugEnabled()) {
				log.debug("Persisting android configurations in Registry");
			}
			String resourcePath = MobileDeviceManagementUtil.getPlatformConfigPath(
					DeviceManagementConstants.
							MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
			StringWriter writer = new StringWriter();
			JAXBContext context = JAXBContext.newInstance(TenantConfiguration.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(tenantConfiguration, writer);

			Resource resource = MobileDeviceManagementUtil.getConfigurationRegistry().newResource();
			resource.setContent(writer.toString());
			resource.setMediaType(MobilePluginConstants.MEDIA_TYPE_XML);
			MobileDeviceManagementUtil.putRegistryResource(resourcePath, resource);
			status = true;
		} catch (MobileDeviceMgtPluginException e) {
			throw new DeviceManagementException(
					"Error occurred while retrieving the Registry instance : " + e.getMessage(), e);
		} catch (RegistryException e) {
			throw new DeviceManagementException(
					"Error occurred while persisting the Registry resource of Android Configuration : " + e.getMessage(), e);
		} catch (JAXBException e) {
			throw new DeviceManagementException(
					"Error occurred while parsing the Android configuration : " + e.getMessage(), e);
		}
		return status;
	}

	@Override
	public TenantConfiguration getConfiguration() throws DeviceManagementException {
		Resource resource;
		try {
			String androidRegPath =
					MobileDeviceManagementUtil.getPlatformConfigPath(DeviceManagementConstants.
							                                                 MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
			resource = MobileDeviceManagementUtil.getRegistryResource(androidRegPath);
			if(resource != null){
				JAXBContext context = JAXBContext.newInstance(TenantConfiguration.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				return (TenantConfiguration) unmarshaller.unmarshal(
						new StringReader(new String((byte[]) resource.getContent(), Charset.
								forName(MobilePluginConstants.CHARSET_UTF8))));
			}
			return new TenantConfiguration();
		} catch (MobileDeviceMgtPluginException e) {
			throw new DeviceManagementException(
					"Error occurred while retrieving the Registry instance : " + e.getMessage(), e);
		} catch (JAXBException e) {
			throw new DeviceManagementException(
					"Error occurred while parsing the Android configuration : " + e.getMessage(), e);
		} catch (RegistryException e) {
			throw new DeviceManagementException(
					"Error occurred while retrieving the Registry resource of Android Configuration : " + e.getMessage(), e);
		}
	}

	@Override
	public boolean enrollDevice(Device device) throws DeviceManagementException {
		boolean status = false;
		MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
		try {
			if (log.isDebugEnabled()) {
				log.debug("Enrolling a new Android device : " + device.getDeviceIdentifier());
			}
			boolean isEnrolled = this.isEnrolled(
					new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
			if (isEnrolled) {
				this.modifyEnrollment(device);
			} else {
				AndroidDAOFactory.beginTransaction();
				status = daoFactory.getMobileDeviceDAO().addMobileDevice(mobileDevice);
				AndroidDAOFactory.commitTransaction();
			}
		} catch (MobileDeviceManagementDAOException e) {
			try {
				AndroidDAOFactory.rollbackTransaction();
			} catch (MobileDeviceManagementDAOException mobileDAOEx) {
				String msg = "Error occurred while roll back the device enrol transaction :" +
				             device.toString();
				log.warn(msg, mobileDAOEx);
			}
			String msg =
					"Error while enrolling the Android device : " + device.getDeviceIdentifier();
			log.error(msg, e);
			throw new DeviceManagementException(msg, e);
		}
		return status;
	}

	@Override
	public boolean modifyEnrollment(Device device) throws DeviceManagementException {
		boolean status;
		MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
		try {
			if (log.isDebugEnabled()) {
				log.debug("Modifying the Android device enrollment data");
			}
			AndroidDAOFactory.beginTransaction();
			status = daoFactory.getMobileDeviceDAO().updateMobileDevice(mobileDevice);
			AndroidDAOFactory.commitTransaction();
		} catch (MobileDeviceManagementDAOException e) {
			try {
				AndroidDAOFactory.rollbackTransaction();
			} catch (MobileDeviceManagementDAOException mobileDAOEx) {
				String msg = "Error occurred while roll back the update device transaction :" +
				             device.toString();
				log.warn(msg, mobileDAOEx);
			}
			String msg = "Error while updating the enrollment of the Android device : " +
			             device.getDeviceIdentifier();
			log.error(msg, e);
			throw new DeviceManagementException(msg, e);
		}
		return status;
	}

	@Override
	public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
		boolean status;
		try {
			if (log.isDebugEnabled()) {
				log.debug("Dis-enrolling Android device : " + deviceId);
			}
			AndroidDAOFactory.beginTransaction();
			status = daoFactory.getMobileDeviceDAO().deleteMobileDevice(deviceId.getId());
			AndroidDAOFactory.commitTransaction();
		} catch (MobileDeviceManagementDAOException e) {
			try {
				AndroidDAOFactory.rollbackTransaction();
			} catch (MobileDeviceManagementDAOException mobileDAOEx) {
				String msg = "Error occurred while roll back the device dis enrol transaction :" +
				             deviceId.toString();
				log.warn(msg, mobileDAOEx);
			}
			String msg = "Error while removing the Android device : " + deviceId.getId();
			log.error(msg, e);
			throw new DeviceManagementException(msg, e);
		}
		return status;
	}

	@Override
	public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
		boolean isEnrolled = false;
		try {
			if (log.isDebugEnabled()) {
				log.debug("Checking the enrollment of Android device : " + deviceId.getId());
			}
			MobileDevice mobileDevice =
					daoFactory.getMobileDeviceDAO().getMobileDevice(deviceId.getId());
			if (mobileDevice != null) {
				isEnrolled = true;
			}
		} catch (MobileDeviceManagementDAOException e) {
			String msg = "Error while checking the enrollment status of Android device : " +
			             deviceId.getId();
			log.error(msg, e);
			throw new DeviceManagementException(msg, e);
		}
		return isEnrolled;
	}

	@Override
	public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
		return true;
	}

	@Override
	public boolean setActive(DeviceIdentifier deviceId, boolean status)
			throws DeviceManagementException {
		return true;
	}

	@Override
	public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
		Device device;
		try {
			if (log.isDebugEnabled()) {
				log.debug("Getting the details of Android device : '" + deviceId.getId() + "'");
			}
			MobileDevice mobileDevice = daoFactory.getMobileDeviceDAO().
					getMobileDevice(deviceId.getId());
			device = MobileDeviceManagementUtil.convertToDevice(mobileDevice);
		} catch (MobileDeviceManagementDAOException e) {
			throw new DeviceManagementException(
					"Error occurred while fetching the Android device: '" +
					deviceId.getId() + "'", e);
		}
		return device;
	}

	@Override
	public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType)
			throws DeviceManagementException {
		return true;
	}

	@Override
	public boolean isClaimable(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
		return false;
	}

	@Override
	public boolean setStatus(DeviceIdentifier deviceIdentifier, String currentUser,
	                         EnrolmentInfo.Status status) throws DeviceManagementException {
		return false;
	}

	@Override
	public License getLicense(String languageCode) throws LicenseManagementException {
		return licenseManager.
				getLicense(AndroidDeviceManagementService.DEVICE_TYPE_ANDROID, languageCode);
	}

	@Override
	public void addLicense(License license) throws LicenseManagementException {
		licenseManager.addLicense(AndroidDeviceManagementService.DEVICE_TYPE_ANDROID, license);
	}

	@Override
	public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device)
			throws DeviceManagementException {
		boolean status;
		Device existingDevice = this.getDevice(deviceIdentifier);
		// This object holds the current persisted device object
		MobileDevice existingMobileDevice =
				MobileDeviceManagementUtil.convertToMobileDevice(existingDevice);

		// This object holds the newly received device object from response
		MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);

		// Updating current object features using newer ones
		existingMobileDevice.setLatitude(mobileDevice.getLatitude());
		existingMobileDevice.setLongitude(mobileDevice.getLongitude());
		existingMobileDevice.setDeviceProperties(mobileDevice.getDeviceProperties());

		try {
			if (log.isDebugEnabled()) {
				log.debug(
						"updating the details of Android device : " + device.getDeviceIdentifier());
			}
			AndroidDAOFactory.beginTransaction();
			status = daoFactory.getMobileDeviceDAO().updateMobileDevice(existingMobileDevice);
			AndroidDAOFactory.commitTransaction();
		} catch (MobileDeviceManagementDAOException e) {
			try {
				AndroidDAOFactory.rollbackTransaction();
			} catch (MobileDeviceManagementDAOException e1) {
				log.warn("Error occurred while roll back the update device info transaction : '" +
				         device.toString() + "'", e1);
			}
			throw new DeviceManagementException(
					"Error occurred while updating the Android device: '" +
					device.getDeviceIdentifier() + "'", e);
		}
		return status;
	}

	@Override
	public List<Device> getAllDevices() throws DeviceManagementException {
		List<Device> devices = null;
		try {
			if (log.isDebugEnabled()) {
				log.debug("Fetching the details of all Android devices");
			}
			List<MobileDevice> mobileDevices =
					daoFactory.getMobileDeviceDAO().getAllMobileDevices();
			if (mobileDevices != null) {
				devices = new ArrayList<>();
				for (MobileDevice mobileDevice : mobileDevices) {
					devices.add(MobileDeviceManagementUtil.convertToDevice(mobileDevice));
				}
			}
		} catch (MobileDeviceManagementDAOException e) {
			throw new DeviceManagementException("Error occurred while fetching all Android devices",
			                                    e);
		}
		return devices;
	}

}
