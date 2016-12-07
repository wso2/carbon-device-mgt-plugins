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
package org.wso2.carbon.device.mgt.mobile.windows.impl.config.datasource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "DataSourceConfigurations")
@XmlAccessorType(XmlAccessType.FIELD)
public class MobileDataSourceConfigurations {

    @XmlElement(name = "DataSourceConfiguration", nillable = true)
    private List<MobileDataSourceConfig> mobileDataSourceConfigs;

    public List<MobileDataSourceConfig> getMobileDataSourceConfigs() {
        return mobileDataSourceConfigs;
    }

    public void setMobileDataSourceConfigs(List<MobileDataSourceConfig> mobileDataSourceConfigs) {
        this.mobileDataSourceConfigs = mobileDataSourceConfigs;
    }

}
