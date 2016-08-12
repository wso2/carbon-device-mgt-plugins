/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.services.android.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(
        name = "PlatformConfiguration"
)
@XmlAccessorType(XmlAccessType.NONE)
@ApiModel(
        value = "PlatformConfiguration",
        description = "This class carries all information related to a Tenant configuration"
)
public class AndroidPlatformConfiguration implements Serializable {
    public static final int INVALID_NOTIFIER_FREQUENCY = -1;
    @XmlElement(
            name = "type"
    )
    @ApiModelProperty(
            name = "type",
            value = "type of device",
            required = true
    )
    @Size(min = 2, max = 10)
    private String type;
    @ApiModelProperty(
            name = "configuration",
            value = "List of Configuration Entries",
            required = true
    )
    @XmlElement(
            name = "configuration"
    )
    private List<ConfigurationEntry> configuration;

    public AndroidPlatformConfiguration() {
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ConfigurationEntry> getConfiguration() {
        return this.configuration;
    }

    public void setConfiguration(List<ConfigurationEntry> configuration) {
        this.configuration = configuration;
    }
}
