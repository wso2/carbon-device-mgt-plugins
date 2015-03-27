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

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.FeatureDAO;
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.FeatureManagementDAOFactory;

import java.util.List;

public class IOSFeatureManager implements FeatureManager {

    private FeatureDAO featureDAO;

    public IOSFeatureManager() {
        this.featureDAO = FeatureManagementDAOFactory.getFeatureDAO();
    }

    @Override
    public boolean addFeature(Feature feature) throws DeviceManagementException {
        return false;
    }

    @Override
    public Feature getFeature(String s) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Feature> getFeatures() throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean removeFeature(String s) throws DeviceManagementException {
        return false;
    }

}
