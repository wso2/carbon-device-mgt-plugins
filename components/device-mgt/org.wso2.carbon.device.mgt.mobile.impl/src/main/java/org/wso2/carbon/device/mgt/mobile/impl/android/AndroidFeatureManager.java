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
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.mobile.dao.AbstractMobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dao.MobileFeatureDAO;
import org.wso2.carbon.device.mgt.mobile.dto.MobileFeature;
import org.wso2.carbon.device.mgt.mobile.impl.android.dao.AndroidDAOFactory;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;

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
        List<MobileFeature> mobileFeatures = new ArrayList<MobileFeature>();
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
        List<Feature> featureList = new ArrayList<Feature>();
        try {
            List<MobileFeature> mobileFeatures = featureDAO.getAllFeatures();
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
        return supportedFeatures;
    }
}