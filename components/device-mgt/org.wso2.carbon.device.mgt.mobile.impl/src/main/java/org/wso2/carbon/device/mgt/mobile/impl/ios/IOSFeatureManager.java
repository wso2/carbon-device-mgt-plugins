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
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.FeatureDAO;
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.FeatureManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.FeatureManagementDAOFactory;

import java.util.List;

public class IOSFeatureManager implements FeatureManager {

    private static final Log log = LogFactory.getLog(IOSFeatureManager.class);

    private FeatureDAO featureDAO;

    public IOSFeatureManager() {
        this.featureDAO = FeatureManagementDAOFactory.getFeatureDAO();
    }

    @Override
    public boolean addFeature(Feature feature) throws DeviceManagementException {
        try {
            FeatureManagementDAOFactory.beginTransaction();
            featureDAO.addFeature(feature);
            FeatureManagementDAOFactory.commitTransaction();
            return true;
        } catch (FeatureManagementDAOException e) {
            try {
                FeatureManagementDAOFactory.rollbackTransaction();
            } catch (FeatureManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e);
            }
            throw new DeviceManagementException("Error occurred while adding the feature", e);
        }
    }

    @Override
    public Feature getFeature(String name) throws DeviceManagementException {
        try {
            FeatureManagementDAOFactory.beginTransaction();
            Feature feature = featureDAO.getFeature(name);
            FeatureManagementDAOFactory.commitTransaction();
            return feature;
        } catch (FeatureManagementDAOException e) {
            try {
                FeatureManagementDAOFactory.rollbackTransaction();
            } catch (FeatureManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e);
            }
            throw new DeviceManagementException("Error occurred while retrieving the feature", e);
        }
    }

    @Override
    public List<Feature> getFeatures() throws DeviceManagementException {
        try {
            FeatureManagementDAOFactory.beginTransaction();
            List<Feature> features = featureDAO.getFeatures();
            FeatureManagementDAOFactory.commitTransaction();
            return features;
        } catch (FeatureManagementDAOException e) {
            try {
                FeatureManagementDAOFactory.rollbackTransaction();
            } catch (FeatureManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e);
            }
            throw new DeviceManagementException("Error occurred while retrieving the list of features registered " +
                    "for Android platform", e);
        }
    }

    @Override
    public boolean removeFeature(String name) throws DeviceManagementException {
        try {
            FeatureManagementDAOFactory.beginTransaction();
            featureDAO.removeFeature(name);
            FeatureManagementDAOFactory.commitTransaction();
            return true;
        } catch (FeatureManagementDAOException e) {
            try {
                FeatureManagementDAOFactory.rollbackTransaction();
            } catch (FeatureManagementDAOException e1) {
                log.warn("Error occurred while roll-backing the transaction", e);
            }
            throw new DeviceManagementException("Error occurred while removing the feature", e);
        }
    }
}
