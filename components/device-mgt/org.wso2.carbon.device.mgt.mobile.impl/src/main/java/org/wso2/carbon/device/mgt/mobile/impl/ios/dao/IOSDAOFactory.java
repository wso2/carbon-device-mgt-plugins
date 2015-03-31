/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.wso2.carbon.device.mgt.mobile.impl.ios.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.mobile.config.datasource.MobileDataSourceConfig;
import org.wso2.carbon.device.mgt.mobile.dao.*;
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.impl.FeatureDAOImpl;
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.impl.IOSDeviceDAOImpl;

public class IOSDAOFactory extends MobileDeviceManagementDAOFactory {

    private static final Log log = LogFactory.getLog(IOSDAOFactory.class);

    public static void init(MobileDataSourceConfig config) {
        dataSource = resolveDataSource(config);
    }

    @Override
    public MobileDeviceDAO getMobileDeviceDAO() {
        return new IOSDeviceDAOImpl(dataSource);
    }

    @Override
    public MobileOperationDAO getMobileOperationDAO() {
        return null;
    }

    @Override
    public MobileOperationPropertyDAO getMobileOperationPropertyDAO() {
        return null;
    }

    @Override
    public MobileDeviceOperationMappingDAO getMobileDeviceOperationDAO() {
        return null;
    }

    @Override
    public MobileFeatureDAO getMobileFeatureDao() {
        return new FeatureDAOImpl();
    }

    public MobileFeaturePropertyDAO getFeaturePropertyDAO() {
        return null;
    }
}
