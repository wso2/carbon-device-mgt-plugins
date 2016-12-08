/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeviceManagementConfiguration")
public class DeviceManagementConfiguration {

    private EventListenerConfiguration eventListenerConfiguration;

    private static final Log log = LogFactory.getLog(DeviceManagementConfiguration.class);

    private DeviceManagementConfiguration() {
    }

    @XmlElement(name = "EventListenerConfiguration", required = false)
    public EventListenerConfiguration getEventListenerConfiguration() {
        return eventListenerConfiguration;
    }

    public void setEventListenerConfiguration(EventListenerConfiguration eventListenerConfiguration) {
        this.eventListenerConfiguration = eventListenerConfiguration;
    }
}
