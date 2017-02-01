/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto;

import org.wso2.carbon.device.mgt.common.Feature;

import java.io.Serializable;
import java.util.List;

/**
 * The DTO class of device.
 */
public class Device implements Serializable {
    private static final long serialVersionUID = 1998101711L;
    private int id;
    private String name;
    private String type;
    private String description;
    private String deviceIdentifier;
    private EnrolmentInfo enrolmentInfo;

    public Device() {
    }

    public Device(String name, String type, String description, String deviceId, EnrolmentInfo enrolmentInfo,
                  List<Feature> features, List<Property> properties) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.deviceIdentifier = deviceId;
        this.enrolmentInfo = enrolmentInfo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public EnrolmentInfo getEnrolmentInfo() {
        return enrolmentInfo;
    }

    public void setEnrolmentInfo(EnrolmentInfo enrolmentInfo) {
        this.enrolmentInfo = enrolmentInfo;
    }

    public static class Property {

        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Override
    public String toString() {
        return "device [" +
                "name=" + name + ";" +
                "type=" + type + ";" +
                "description=" + description + ";" +
                "identifier=" + deviceIdentifier + ";" +
                "EnrolmentInfo[" +
                "owner=" + enrolmentInfo.getOwner() + ";" +
                "ownership=" + enrolmentInfo.getOwnership() + ";" +
                "status=" + enrolmentInfo.getStatus() + ";" +
                "]" +
                "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof org.wso2.carbon.device.mgt.common.Device))
            return false;

        org.wso2.carbon.device.mgt.common.Device device = (org.wso2.carbon.device.mgt.common.Device) o;

        return getDeviceIdentifier().equals(device.getDeviceIdentifier());

    }

    @Override
    public int hashCode() {
        return getDeviceIdentifier().hashCode();
    }
}
