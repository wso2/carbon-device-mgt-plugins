/*
 * Copyright (c) 2017, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.services.android.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XmlRootElement
/**
 * Store Device state event/data for androidDevice.
 */
@ApiModel(value = "DeviceState",
        description = "This class carries all information related to device state.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceState {

    @XmlElementWrapper(required = true, name = "values")
    @ApiModelProperty(name = "values", value = "Device and its statuses.", required = true)
    private Map<String, Object> values;

    /** The id. */
    @XmlElement(required = false, name = "id")
    @ApiModelProperty(name = "id", value = "Identification code.", required = true)
    private String id;

    /**
     * Gets the values.
     * @return the values
     */
    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * Sets the values.
     * @param values the values
     */
    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    /**
     * Sets the id.
     * @param id the new id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the id.
     * @return the id
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString(){
        List<String> valueList = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            valueList.add(entry.getKey() + ":" + entry.getValue());
        }
        return valueList.toString();

    }

}
