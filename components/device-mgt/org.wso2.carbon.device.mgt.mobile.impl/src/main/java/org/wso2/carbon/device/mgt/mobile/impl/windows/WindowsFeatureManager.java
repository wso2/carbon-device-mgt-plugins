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
package org.wso2.carbon.device.mgt.mobile.impl.windows;

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
import org.wso2.carbon.device.mgt.mobile.impl.windows.dao.WindowsDAOFactory;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;

import java.util.ArrayList;
import java.util.List;

public class WindowsFeatureManager implements FeatureManager {

    private MobileFeatureDAO featureDAO;
    private static final Log log = LogFactory.getLog(WindowsFeatureManager.class);

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
        }
    }

    @Override
    public boolean addFeatures(List<Feature> list) throws DeviceManagementException {
        return false;
    }

    @Override
    public Feature getFeature(String name) throws DeviceManagementException {
        try {
            MobileFeature mobileFeature = featureDAO.getFeatureByCode(name);
            Feature feature =  MobileDeviceManagementUtil.convertToFeature(mobileFeature);
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
	        WindowsDAOFactory.beginTransaction();
            featureDAO.deleteFeatureByCode(code);
	        WindowsDAOFactory.commitTransaction();
            status = true;
        } catch (MobileDeviceManagementDAOException e) {
	        WindowsDAOFactory.rollbackTransaction();
            throw new DeviceManagementException("Error occurred while removing the feature", e);
        }
        return status;
    }

    @Override
    public boolean addSupportedFeaturesToDB() throws DeviceManagementException {
        return false;
    }

}