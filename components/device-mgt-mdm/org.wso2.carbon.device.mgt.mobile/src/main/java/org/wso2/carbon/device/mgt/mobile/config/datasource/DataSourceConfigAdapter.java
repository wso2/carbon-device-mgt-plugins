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

package org.wso2.carbon.device.mgt.mobile.config.datasource;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSourceConfigAdapter
        extends XmlAdapter<MobileDataSourceConfigurations, Map<String, MobileDataSourceConfig>> {

    @Override
    public Map<String, MobileDataSourceConfig> unmarshal(MobileDataSourceConfigurations mobileDataSourceConfigurations)
            throws Exception {

        Map<String, MobileDataSourceConfig> mobileDataSourceConfigMap = new HashMap<String, MobileDataSourceConfig>();
        for (MobileDataSourceConfig mobileDataSourceConfig : mobileDataSourceConfigurations
                .getMobileDataSourceConfigs()) {
            mobileDataSourceConfigMap.put(mobileDataSourceConfig.getType(), mobileDataSourceConfig);
        }
        return mobileDataSourceConfigMap;
    }

    @Override
    public MobileDataSourceConfigurations marshal(Map<String, MobileDataSourceConfig> mobileDataSourceConfigMap)
            throws Exception {

        MobileDataSourceConfigurations mobileDataSourceConfigurations = new MobileDataSourceConfigurations();
        mobileDataSourceConfigurations.setMobileDataSourceConfigs(
                (List<MobileDataSourceConfig>) mobileDataSourceConfigMap.values());

        return mobileDataSourceConfigurations;
    }
}
