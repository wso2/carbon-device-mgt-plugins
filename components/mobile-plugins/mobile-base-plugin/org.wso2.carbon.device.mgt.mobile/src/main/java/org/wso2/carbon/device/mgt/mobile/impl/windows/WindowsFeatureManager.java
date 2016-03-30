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

package org.wso2.carbon.device.mgt.mobile.impl.windows;

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dao.MobileFeatureDAO;
import org.wso2.carbon.device.mgt.mobile.dto.MobileFeature;
import org.wso2.carbon.device.mgt.mobile.impl.windows.dao.WindowsDAOFactory;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;

import java.util.ArrayList;
import java.util.List;

public class WindowsFeatureManager implements FeatureManager {

    private MobileFeatureDAO featureDAO;

    public WindowsFeatureManager() {
        MobileDeviceManagementDAOFactory daoFactory = new WindowsDAOFactory();
        this.featureDAO = daoFactory.getMobileFeatureDAO();
    }

    @Override
    public boolean addFeature(Feature feature) throws DeviceManagementException {
        try {
            WindowsDAOFactory.beginTransaction();
            MobileFeature mobileFeature = MobileDeviceManagementUtil.convertToMobileFeature(feature);
            featureDAO.addFeature(mobileFeature);
            WindowsDAOFactory.commitTransaction();
            return true;
        } catch (MobileDeviceManagementDAOException e) {
            WindowsDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while adding the feature", e);
        } finally {
            WindowsDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean addFeatures(List<Feature> features) throws DeviceManagementException {
        List<MobileFeature> mobileFeatures = new ArrayList<MobileFeature>(features.size());
        for (Feature feature : features) {
            mobileFeatures.add(MobileDeviceManagementUtil.convertToMobileFeature(feature));
        }
        try {
            WindowsDAOFactory.beginTransaction();
            featureDAO.addFeatures(mobileFeatures);
            WindowsDAOFactory.commitTransaction();
            return true;
        } catch (MobileDeviceManagementDAOException e) {
            WindowsDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while adding the features", e);
        } finally {
            WindowsDAOFactory.closeConnection();
        }
    }

    @Override
    public Feature getFeature(String name) throws DeviceManagementException {
        try {
            WindowsDAOFactory.openConnection();
            MobileFeature mobileFeature = featureDAO.getFeatureByCode(name);
            Feature feature = MobileDeviceManagementUtil.convertToFeature(mobileFeature);
            return feature;
        } catch (MobileDeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the feature", e);
        } finally {
            WindowsDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Feature> getFeatures() throws DeviceManagementException {

        try {
            WindowsDAOFactory.openConnection();
            List<MobileFeature> mobileFeatures = featureDAO.getAllFeatures();
            List<Feature> featureList = new ArrayList<Feature>(mobileFeatures.size());
            for (MobileFeature mobileFeature : mobileFeatures) {
                featureList.add(MobileDeviceManagementUtil.convertToFeature(mobileFeature));
            }
            return featureList;
        } catch (MobileDeviceManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the list of features registered for " +
                    "Windows platform", e);
        } finally {
            WindowsDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean removeFeature(String code) throws DeviceManagementException {
        boolean status;
        try {
            WindowsDAOFactory.beginTransaction();
            featureDAO.deleteFeatureByCode(code);
            WindowsDAOFactory.commitTransaction();
            status = true;
        } catch (MobileDeviceManagementDAOException e) {
            WindowsDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while removing the feature", e);
        } finally {
            WindowsDAOFactory.closeConnection();
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

    /**
     * Get supported Windows features.
     *
     * @return Supported features.
     */
    public static List<Feature> getSupportedFeatures() {
        List<Feature> supportedFeatures = new ArrayList<Feature>();
        Feature feature = new Feature();
        feature.setCode("DEVICE_LOCK");
        feature.setName("Device Lock");
        feature.setDescription("Lock the device");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("CAMERA");
        feature.setName("camera");
        feature.setDescription("Enable or disable camera");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("DEVICE_INFO");
        feature.setName("Device info");
        feature.setDescription("Request device information");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("WIPE_DATA");
        feature.setName("Wipe Data");
        feature.setDescription("Factory reset the device");
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
        feature.setCode("DISENROLL");
        feature.setName("DisEnroll");
        feature.setDescription("DisEnroll the device");
        supportedFeatures.add(feature);
        feature = new Feature();
        feature.setCode("LOCK_RESET");
        feature.setName("LockReset");
        feature.setDescription("Lock Reset device");
        supportedFeatures.add(feature);
        return supportedFeatures;
    }
}
