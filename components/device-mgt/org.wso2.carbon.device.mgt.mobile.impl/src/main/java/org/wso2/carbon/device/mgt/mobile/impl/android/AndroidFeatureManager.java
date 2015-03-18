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

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManagementException;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.mobile.impl.android.dao.FeatureDAO;
import org.wso2.carbon.device.mgt.mobile.impl.android.dao.FeatureManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.impl.android.dao.FeatureManagementDAOFactory;

import java.util.List;

public class AndroidFeatureManager implements FeatureManager {

    private FeatureDAO featureDAO;

    public AndroidFeatureManager() {
        this.featureDAO = FeatureManagementDAOFactory.getFeatureDAO();
    }

    @Override
    public boolean addFeature(Feature feature) throws DeviceManagementException {
        try {
            featureDAO.addFeature(feature);
            return true;
        } catch (FeatureManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while adding the feature", e);
        }
    }

    @Override
    public Feature getFeature(String name) throws DeviceManagementException {
        try {
            return featureDAO.getFeature(name);
        } catch (FeatureManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the feature", e);
        }
    }

    @Override
    public List<Feature> getFeatures() throws DeviceManagementException {
        try {
            return featureDAO.getFeatures();
        } catch (FeatureManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while retrieving the list of features registered " +
                    "for Android platform", e);
        }
    }

    @Override
    public boolean removeFeature(String name) throws DeviceManagementException {
        try {
            featureDAO.removeFeature(name);
            return true;
        } catch (FeatureManagementDAOException e) {
            throw new DeviceManagementException("Error occurred while removing the feature", e);
        }
    }

}
