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
package org.wso2.carbon.device.mgt.mobile.impl.android.dao.impl;

import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.mobile.impl.android.dao.FeatureDAO;
import org.wso2.carbon.device.mgt.mobile.impl.android.dao.FeatureManagementDAOException;

import java.util.List;

public class FeatureDAOImpl implements FeatureDAO {

    @Override
    public void addFeature(Feature feature) throws FeatureManagementDAOException {

    }

    @Override
    public void removeFeature(String name) throws FeatureManagementDAOException {

    }

    @Override
    public Feature getFeature(String name) throws FeatureManagementDAOException {
        return null;
    }

    @Override
    public List<Feature> getFeatures() throws FeatureManagementDAOException {
        return null;
    }

}
