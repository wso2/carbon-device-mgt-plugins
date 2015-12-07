/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.iot.config.server.datasource;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for DeviceUserValidatorConfig complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="DeviceMgtServerInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Domain" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeviceMgtServerInfo", propOrder = {
        "name",
        "domain"
})
public class DeviceMgtServerInfo {
    @XmlElement(name = "Name", required = true)
    protected String name;

    @XmlElement(name = "Domain", required = true)
    protected String domain;

    /**
     * Gets the domain of the current DeviceManagement Server Instance
     * @return the domain of the current device-management server instance. [eg: wso2.iotserver.com]
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets the domain of the current DeviceManagement Server Instance
     * @param domain the domain of the current device-management server instance. [eg: wso2.iotserver.com]
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Gets the name of the current DeviceManagement Server Instance
     * @return the name of the current device-management server instance. [eg: "WSO2-US-IoT"]
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the current DeviceManagement Server Instance
     * @param name the name of the current device-management server instance. [eg: "WSO2-US-IoT"]
     */
    public void setName(String name) {
        this.name = name;
    }

}
