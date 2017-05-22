/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.device.mgt.mobile.windows.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.extensions.license.mgt.registry.RegistryBasedLicenseManager;
import org.wso2.carbon.device.mgt.mobile.windows.exception.WindowsDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.AbstractMobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.WindowsDAOFactory;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dto.MobileDevice;
import org.wso2.carbon.device.mgt.mobile.windows.impl.util.MobileDeviceManagementUtil;
import org.wso2.carbon.device.mgt.mobile.windows.impl.util.WindowsPluginConstants;
import org.wso2.carbon.device.mgt.mobile.windows.impl.util.WindowsPluginUtils;
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

public class WindowsDeviceManager implements DeviceManager {

    private AbstractMobileDeviceManagementDAOFactory daoFactory;
    private LicenseManager licenseManager;
    private FeatureManager featureManager = new WindowsFeatureManager();
    private static final Log log = LogFactory.getLog(WindowsDeviceManagementService.class);

    public WindowsDeviceManager() {
        this.daoFactory = new WindowsDAOFactory();
        this.licenseManager = new RegistryBasedLicenseManager();

        License defaultLicense = WindowsPluginUtils.getDefaultLicense();

        try {
            if (licenseManager.getLicense(WindowsDeviceManagementService.DEVICE_TYPE_WINDOWS,
                    WindowsPluginConstants.LANGUAGE_CODE_ENGLISH_US) == null) {
                licenseManager.addLicense(WindowsDeviceManagementService.DEVICE_TYPE_WINDOWS, defaultLicense);
            }
            featureManager.addSupportedFeaturesToDB();
        } catch (LicenseManagementException e) {
            log.error("Error occurred while adding default license for Windows devices", e);
        } catch (DeviceManagementException e) {
            log.error("Error occurred while adding supported device features for Windows platform", e);
        }
    }

    @Override
    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    @Override
    public boolean saveConfiguration(PlatformConfiguration tenantConfiguration)
            throws DeviceManagementException {
        boolean status;
        Resource resource;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Persisting windows configurations in Registry");
            }
            String resourcePath = MobileDeviceManagementUtil.getPlatformConfigPath(
                    DeviceManagementConstants.
                            MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(PlatformConfiguration.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(tenantConfiguration, writer);

            resource = MobileDeviceManagementUtil.getConfigurationRegistry().newResource();
            resource.setContent(writer.toString());
            resource.setMediaType(WindowsPluginConstants.MEDIA_TYPE_XML);
            MobileDeviceManagementUtil.putRegistryResource(resourcePath, resource);
            status = true;
        } catch (WindowsDeviceMgtPluginException e) {
            throw new DeviceManagementException(
                    "Error occurred while retrieving the Registry instance : " + e.getMessage(), e);
        } catch (RegistryException e) {
            throw new DeviceManagementException(
                    "Error occurred while persisting the Registry resource of Windows configuration : " + e.getMessage(), e);
        } catch (JAXBException e) {
            throw new DeviceManagementException(
                    "Error occurred while parsing the Windows configuration : " + e.getMessage(), e);
        }
        return status;
    }

    @Override
    public PlatformConfiguration getConfiguration() throws DeviceManagementException {
        Resource resource;
        try {
            String windowsTenantRegistryPath =
                    MobileDeviceManagementUtil.getPlatformConfigPath(DeviceManagementConstants.
                            MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
            resource = MobileDeviceManagementUtil.getRegistryResource(windowsTenantRegistryPath);
            if (resource != null) {
                JAXBContext context = JAXBContext.newInstance(PlatformConfiguration.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                return (PlatformConfiguration) unmarshaller.unmarshal(
                        new StringReader(new String((byte[]) resource.getContent(), Charset.
                                forName(WindowsPluginConstants.CHARSET_UTF8))));
            }
            return null;
        } catch (WindowsDeviceMgtPluginException e) {
            throw new DeviceManagementException(
                    "Error occurred while retrieving the Registry instance : " + e.getMessage(), e);
        } catch (JAXBException e) {
            throw new DeviceManagementException(
                    "Error occurred while parsing the Windows configuration : " + e.getMessage(), e);
        } catch (RegistryException e) {
            throw new DeviceManagementException(
                    "Error occurred while retrieving the Registry resource of Windows configuration : " + e.getMessage(), e);
        }
    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        boolean status;
        MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Modifying the Windows device enrollment data");
            }
            WindowsDAOFactory.beginTransaction();
            status = daoFactory.getMobileDeviceDAO().updateMobileDevice(mobileDevice);
            WindowsDAOFactory.commitTransaction();
        } catch (MobileDeviceManagementDAOException e) {
            WindowsDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error while updating the enrollment of the Windows device : " +
                    device.getDeviceIdentifier(), e);
        } finally {
            WindowsDAOFactory.closeConnection();
        }
        return status;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        //Here we don't have anything specific to do. Hence returning.
        return true;
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        boolean isEnrolled = false;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Checking the enrollment of Windows device : " + deviceId.getId());
            }
            MobileDevice mobileDevice =
                    daoFactory.getMobileDeviceDAO().getMobileDevice(deviceId.getId());
            if (mobileDevice != null) {
                isEnrolled = true;
            }
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error while checking the enrollment status of Windows device : " +
                    deviceId.getId();
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

    public List<Device> getAllDevices() throws DeviceManagementException {
        List<Device> devices = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Fetching the details of all Windows devices");
            }
            WindowsDAOFactory.openConnection();
            List<MobileDevice> mobileDevices = daoFactory.getMobileDeviceDAO().getAllMobileDevices();
            if (mobileDevices != null) {
                devices = new ArrayList<>(mobileDevices.size());
                for (MobileDevice mobileDevice : mobileDevices) {
                    devices.add(MobileDeviceManagementUtil.convertToDevice(mobileDevice));
                }
            }
        } catch (MobileDeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while fetching all Windows devices", e);
        } finally {
            WindowsDAOFactory.closeConnection();
        }
        return devices;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        Device device = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting the details of Windows device : '" + deviceId.getId() + "'");
            }
            WindowsDAOFactory.openConnection();
            MobileDevice mobileDevice = daoFactory.getMobileDeviceDAO().
                    getMobileDevice(deviceId.getId());
            device = MobileDeviceManagementUtil.convertToDevice(mobileDevice);
        } catch (MobileDeviceManagementDAOException e) {
            throw new DeviceManagementException(
                    "Error occurred while fetching the Windows device: '" + deviceId.getId() + "'", e);
        } finally {
            WindowsDAOFactory.closeConnection();
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
        return licenseManager.getLicense(WindowsDeviceManagementService.DEVICE_TYPE_WINDOWS, languageCode);
    }

    @Override
    public void addLicense(License license) throws LicenseManagementException {
        licenseManager.addLicense(WindowsDeviceManagementService.DEVICE_TYPE_WINDOWS, license);
    }

    @Override
    public boolean requireDeviceAuthorization() {
        return false;
    }

    @Override
    public PlatformConfiguration getDefaultConfiguration() throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier,
                                    Device device) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        boolean status = false;
        MobileDevice mobileDevice = MobileDeviceManagementUtil.convertToMobileDevice(device);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Enrolling a new windows device : " + device.getDeviceIdentifier());
            }
            boolean isEnrolled = this.isEnrolled(
                    new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            if (isEnrolled) {
                this.modifyEnrollment(device);
            } else {
                WindowsDAOFactory.beginTransaction();
                status = daoFactory.getMobileDeviceDAO().addMobileDevice(mobileDevice);
                WindowsDAOFactory.commitTransaction();
            }
        } catch (MobileDeviceManagementDAOException e) {
            WindowsDAOFactory.rollbackTransaction();
            String msg =
                    "Error while enrolling the windows device : " + device.getDeviceIdentifier();
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

}
