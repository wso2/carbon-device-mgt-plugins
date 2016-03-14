
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DeviceCloudConfig complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="DeviceManagementConfigurations">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ServerInfo" type="{}DeviceMgtServerInfo"/>
 *         &lt;element name="DataStores" type="{}DataStoresConfig"/>
 *         &lt;element name="ControlQueues" type="{}ControlQueuesConfig"/>
 *         &lt;element name="Security" type="{}SecurityConfig"/>
 *         &lt;element name="DeviceUserValidator" type="{}DeviceUserValidatorConfig"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeviceManagementConfigurations", propOrder = {
        "dmServerInfo",
        "controlQueues",
        "security"
})
@XmlRootElement(name = "DeviceManagementConfigurations")
public class DeviceManagementConfiguration {
    @XmlElement(name = "DMServerInfo", required = true)
    protected DeviceMgtServerInfo dmServerInfo;
    @XmlElement(name = "ControlQueues", required = true)
    protected ControlQueuesConfig controlQueues;
    @XmlElement(name = "Security", required = true)

    /**
     * Gets the value of the ServerInfo property of the device management configurations manager
     * @return possible object is
     * {@link DeviceMgtServerInfo }
     */
    public DeviceMgtServerInfo getDmServerInfo() {
        return dmServerInfo;
    }

    /**
     * Sets the value of the ServerInfo property.
     *
     * @param value allowed object is
     *              {@link DeviceMgtServerInfo }
     */
    public void setDmServerInfo(DeviceMgtServerInfo value) {
        this.dmServerInfo = value;
    }

    /**
     * Gets the value of the controlQueues property.
     *
     * @return possible object is
     * {@link ControlQueuesConfig }
     */
    public ControlQueuesConfig getControlQueues() {
        return controlQueues;
    }

    /**
     * Sets the value of the controlQueues property.
     *
     * @param value allowed object is
     *              {@link ControlQueuesConfig }
     */
    public void setControlQueues(ControlQueuesConfig value) {
        this.controlQueues = value;
    }
}
