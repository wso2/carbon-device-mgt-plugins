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
package org.wso2.carbon.device.mgt.mobile.android.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.mobile.android.impl.dao.AndroidDAOFactory;
import org.wso2.carbon.device.mgt.mobile.android.impl.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.android.impl.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.android.impl.dao.MobileFeatureDAO;
import org.wso2.carbon.device.mgt.mobile.android.impl.dto.MobileFeature;
import org.wso2.carbon.device.mgt.mobile.android.impl.util.MobileDeviceManagementUtil;

import java.util.ArrayList;
import java.util.List;

public class AndroidFeatureManager implements FeatureManager {

    private MobileFeatureDAO featureDAO;
    private static final Log log = LogFactory.getLog(AndroidFeatureManager.class);

    public AndroidFeatureManager() {
        MobileDeviceManagementDAOFactory daoFactory = new AndroidDAOFactory();
        this.featureDAO = daoFactory.getMobileFeatureDAO();
    }

    @Override
    public boolean addFeature(Feature feature) throws DeviceManagementException {
        try {
            AndroidDAOFactory.beginTransaction();
            MobileFeature mobileFeature = MobileDeviceManagementUtil.convertToMobileFeature(feature);
            featureDAO.addFeature(mobileFeature);
            AndroidDAOFactory.commitTransaction();
            return true;
        } catch (MobileDeviceManagementDAOException e) {
            try {
                AndroidDAOFactory.rollbackTransaction();
            } catch (MobileDeviceManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e);
            }
            throw new DeviceManagementException("Error occurred while adding the feature", e);
        }
    }

    @Override
    public boolean addFeatures(List<Feature> features) throws DeviceManagementException {
        List<MobileFeature> mobileFeatures = new ArrayList<MobileFeature>(features.size());
        for (Feature feature : features) {
            mobileFeatures.add(MobileDeviceManagementUtil.convertToMobileFeature(feature));
        }
        try {
            AndroidDAOFactory.beginTransaction();
            featureDAO.addFeatures(mobileFeatures);
            AndroidDAOFactory.commitTransaction();
            return true;
        } catch (MobileDeviceManagementDAOException e) {
            try {
                AndroidDAOFactory.rollbackTransaction();
            } catch (MobileDeviceManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e);
            }
            throw new DeviceManagementException("Error occurred while adding the features", e);
        }
    }

    @Override
    public Feature getFeature(String name) throws DeviceManagementException {
        try {
            MobileFeature mobileFeature = featureDAO.getFeatureByCode(name);
            Feature feature = MobileDeviceManagementUtil.convertToFeature(mobileFeature);
            return feature;
        } catch (MobileDeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the feature", e);
        }
    }

    @Override
    public List<Feature> getFeatures() throws DeviceManagementException {
        try {
            List<MobileFeature> mobileFeatures = featureDAO.getAllFeatures();
            List<Feature> featureList = new ArrayList<Feature>(mobileFeatures.size());
            for (MobileFeature mobileFeature : mobileFeatures) {
                featureList.add(MobileDeviceManagementUtil.convertToFeature(mobileFeature));
            }
            return featureList;
        } catch (MobileDeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the list of features registered for " +
                                                "Android platform", e);
        }
    }

    @Override
    public boolean removeFeature(String code) throws DeviceManagementException {
        boolean status;
        try {
            AndroidDAOFactory.beginTransaction();
            featureDAO.deleteFeatureByCode(code);
            AndroidDAOFactory.commitTransaction();
            status = true;
        } catch (MobileDeviceManagementDAOException e) {
            try {
                AndroidDAOFactory.rollbackTransaction();
            } catch (MobileDeviceManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e);
            }
            throw new DeviceManagementException("Error occurred while removing the feature", e);
        }
        return status;
    }

    @Override
    public boolean addSupportedFeaturesToDB() throws DeviceManagementException {
        synchronized (this) {
            List<Feature> supportedFeatures = getSupportedFeatures();
            List<Feature> existingFeatures = this.getFeatures();
            List<Feature> missingFeatures = MobileDeviceManagementUtil.
                                                           getMissingFeatures(supportedFeatures, existingFeatures);
            if (missingFeatures.size() > 0) {
                return this.addFeatures(missingFeatures);
            }
            return true;
        }
    }

    //Get the supported feature list.
    private static List<Feature> getSupportedFeatures() {
        List<Feature> supportedFeatures = new ArrayList<Feature>();
        Feature feature = new Feature();
        feature.setCode("DEVICE_LOCK");
        feature.setName("Device Lock");
        feature.setDescription("Lock the device");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("DEVICE_LOCATION");
        feature.setName("Location");
        feature.setDescription("Request coordinates of device location");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("WIFI");
        feature.setName("wifi");
        feature.setDescription("Setting up wifi configuration");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("CAMERA");
        feature.setName("camera");
        feature.setDescription("Enable or disable camera");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("EMAIL");
        feature.setName("Email");
        feature.setDescription("Configure email settings");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("DEVICE_MUTE");
        feature.setName("mute");
        feature.setDescription("Enable mute in the device");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("DEVICE_INFO");
        feature.setName("Device info");
        feature.setDescription("Request device information");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("ENTERPRISE_WIPE");
        feature.setName("Enterprise Wipe");
        feature.setDescription("Remove enterprise applications");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("CLEAR_PASSWORD");
        feature.setName("Clear Password");
        feature.setDescription("Clear current password");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("WIPE_DATA");
        feature.setName("Wipe Data");
        feature.setDescription("Factory reset the device");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("APPLICATION_LIST");
        feature.setName("Application List");
        feature.setDescription("Request list of current installed applications");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("CHANGE_LOCK_CODE");
        feature.setName("Change Lock-code");
        feature.setDescription("Change current lock code");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("INSTALL_APPLICATION");
        feature.setName("Install App");
        feature.setDescription("Install Enterprise or Market application");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("UNINSTALL_APPLICATION");
        feature.setName("Uninstall App");
        feature.setDescription("Uninstall application");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("BLACKLIST_APPLICATIONS");
        feature.setName("Blacklist app");
        feature.setDescription("Blacklist applications");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("ENCRYPT_STORAGE");
        feature.setName("Encrypt storage");
        feature.setDescription("Encrypt storage");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("DEVICE_RING");
        feature.setName("Ring");
        feature.setDescription("Ring the device");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("PASSCODE_POLICY");
        feature.setName("Password Policy");
        feature.setDescription("Set passcode policy");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("NOTIFICATION");
        feature.setName("Message");
        feature.setDescription("Send message");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DEVICE_REBOOT");
        feature.setName("Reboot");
        feature.setDescription("Reboot the device");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("UPGRADE_FIRMWARE");
        feature.setName("Upgrade Firmware");
        feature.setDescription("Upgrade Firmware");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("VPN");
        feature.setName("Configure VPN");
        feature.setDescription("Configure VPN settings");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_ADJUST_VOLUME");
        feature.setName("Adjust Volume");
        feature.setDescription("allow or disallow user to change volume");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_CONFIG_BLUETOOTH");
        feature.setName("Disallow bluetooth configuration");
        feature.setDescription("allow or disallow user to change bluetooth configurations");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_CONFIG_CELL_BROADCASTS");
        feature.setName("Disallow cell broadcast configuration");
        feature.setDescription("allow or disallow user to change cell broadcast configurations");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_CONFIG_CREDENTIALS");
        feature.setName("Disallow credential configuration");
        feature.setDescription("allow or disallow user to change user credentials");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_CONFIG_MOBILE_NETWORKS");
        feature.setName("Disallow mobile network configure");
        feature.setDescription("allow or disallow user to change mobile networks configurations");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_CONFIG_TETHERING");
        feature.setName("Disallow tethering configuration");
        feature.setDescription("allow or disallow user to change tethering configurations");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_CONFIG_VPN");
        feature.setName("Disallow VPN configuration");
        feature.setDescription("allow or disallow user to change VPN configurations");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_CONFIG_WIFI");
        feature.setName("Disallow WIFI configuration");
        feature.setDescription("allow or disallow user to change WIFI configurations");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_APPS_CONTROL");
        feature.setName("Disallow APP control configuration");
        feature.setDescription("allow or disallow user to change app control");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_CREATE_WINDOWS");
        feature.setName("Disallow window creation");
        feature.setDescription("allow or disallow window creation");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_APPS_CONTROL");
        feature.setName("Disallow APP control configuration");
        feature.setDescription("allow or disallow user to change app control configurations");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_CROSS_PROFILE_COPY_PASTE");
        feature.setName("Disallow cross profile copy paste");
        feature.setDescription("allow or disallow cross profile copy paste");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_DEBUGGING_FEATURES");
        feature.setName("Disallow debugging features");
        feature.setDescription("allow or disallow debugging features");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_FACTORY_RESET");
        feature.setName("Disallow factory reset");
        feature.setDescription("allow or disallow factory reset");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_ADD_USER");
        feature.setName("Disallow add user");
        feature.setDescription("allow or disallow add user");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_INSTALL_APPS");
        feature.setName("Disallow install apps");
        feature.setDescription("allow or disallow install apps");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_INSTALL_UNKNOWN_SOURCES");
        feature.setName("Disallow install unknown sources");
        feature.setDescription("allow or disallow install unknown sources");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_MODIFY_ACCOUNTS");
        feature.setName("Disallow modify account");
        feature.setDescription("allow or disallow modify account");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_MOUNT_PHYSICAL_MEDIA");
        feature.setName("Disallow mount physical media");
        feature.setDescription("allow or disallow mount physical media.");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_NETWORK_RESET");
        feature.setName("Disallow network reset");
        feature.setDescription("allow or disallow network reset");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_OUTGOING_BEAM");
        feature.setName("Disallow outgoing beam");
        feature.setDescription("allow or disallow outgoing beam.");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_OUTGOING_CALLS");
        feature.setName("Disallow outgoing calls");
        feature.setDescription("allow or disallow outgoing calls");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_REMOVE_USER");
        feature.setName("Disallow remove users");
        feature.setDescription("allow or disallow remove users");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_SAFE_BOOT");
        feature.setName("Disallow safe boot");
        feature.setDescription("allow or disallow safe boot");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_SHARE_LOCATION");
        feature.setName("Disallow share location");
        feature.setDescription("allow or disallow share location.");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_SMS");
        feature.setName("Disallow sms");
        feature.setDescription("allow or disallow sms");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_UNINSTALL_APPS");
        feature.setName("Disallow uninstall app");
        feature.setDescription("allow or disallow uninstall app");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_UNMUTE_MICROPHONE");
        feature.setName("Disallow unmute mic");
        feature.setDescription("allow or disallow unmute mic");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DISALLOW_USB_FILE_TRANSFER");
        feature.setName("Disallow usb file transfer");
        feature.setDescription("allow or disallow usb file transfer");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("ALLOW_PARENT_PROFILE_APP_LINKING");
        feature.setName("Disallow parent profile app linking");
        feature.setDescription("allow or disallow parent profile app linking");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("ENSURE_VERIFY_APPS");
        feature.setName("Disallow ensure verify apps");
        feature.setDescription("allow or disallow ensure verify apps");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("AUTO_TIME");
        feature.setName("Allow auto timing");
        feature.setDescription("allow or disallow auto timing");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("REMOVE_DEVICE_OWNER");
        feature.setName("Remove device owner");
        feature.setDescription("remove device owner");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("LOGCAT");
        feature.setName("Fetch Logcat");
        feature.setDescription("Fetch device logcat");
        supportedFeatures.add(feature);

        feature = new Feature();
        feature.setCode("DEVICE_UNLOCK");
        feature.setName("Device Unlock");
        feature.setDescription("Unlock the device");
        supportedFeatures.add(feature);

        return supportedFeatures;
    }
}