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
package org.wso2.carbon.device.mgt.mobile.impl.ios;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dao.MobileFeatureDAO;
import org.wso2.carbon.device.mgt.mobile.dto.MobileFeature;
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.IOSDAOFactory;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;

import java.util.ArrayList;
import java.util.List;

public class IOSFeatureManager implements FeatureManager {

    private static final Log log = LogFactory.getLog(IOSFeatureManager.class);
    private MobileDeviceManagementDAOFactory mobileDeviceManagementDAOFactory;

    private MobileFeatureDAO featureDAO;

    public IOSFeatureManager() {
        mobileDeviceManagementDAOFactory = new IOSDAOFactory();
        this.featureDAO = mobileDeviceManagementDAOFactory.getMobileFeatureDao();
    }

    @Override
    public boolean addFeature(Feature feature) throws DeviceManagementException {
        try {
            mobileDeviceManagementDAOFactory.beginTransaction();
            MobileFeature mobileFeature = MobileDeviceManagementUtil.convertToMobileFeature(feature);

            try {
                featureDAO.addFeature(mobileFeature);
            } catch (MobileDeviceManagementDAOException e) {
                log.error("error in feature add ", e);
                throw new DeviceManagementException("error in feature add", e);
            }
            mobileDeviceManagementDAOFactory.commitTransaction();
            return true;
        } catch (MobileDeviceManagementDAOException e) {
            try {
                mobileDeviceManagementDAOFactory.rollbackTransaction();
            } catch (MobileDeviceManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e);
            }
            throw new DeviceManagementException("DB transaction error occurred while add the feature", e);
        }
    }

    @Override
    public Feature getFeature(String code) throws DeviceManagementException {
        try {
            MobileFeature mobileFeature = featureDAO.getFeatureByCode(code);
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
            throw new DeviceManagementException("Error occurred while retrieving the list of features registered " +
                    "for Android platform", e);
        }
    }

    @Override
    public boolean removeFeature(String name) throws DeviceManagementException {
        try {
            mobileDeviceManagementDAOFactory.beginTransaction();
            featureDAO.deleteFeatureByCode(name);
            mobileDeviceManagementDAOFactory.commitTransaction();
            return true;
        } catch (MobileDeviceManagementDAOException e) {
            try {
                mobileDeviceManagementDAOFactory.rollbackTransaction();
            } catch (MobileDeviceManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e);
            }
            throw new DeviceManagementException("Error occurred while removing the feature", e);
        }
    }
}
