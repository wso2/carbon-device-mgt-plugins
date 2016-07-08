/*
 * Copyright (c) 2016, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mdm.services.android.bean.wrapper;

import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

public class AndroidDevice implements Serializable {
    private static final long serialVersionUID = 1998101711L;

    @ApiModelProperty(
            name = "name",
            value = "The device name that can be set on the device by the device user.",
            required = true
    )
    @Size(min = 2, max = 45)
    @Pattern(regexp = "^[A-Za-z0-9]*$")
    private String name;
    @ApiModelProperty(
            name = "type",
            value = "The OS type of the device.",
            required = true
    )
    @NotNull
    @Size(min = 2, max = 45)
    @Pattern(regexp = "^[A-Za-z]*$")
    private String type;
    @ApiModelProperty(
            name = "description",
            value = "Additional information on the device.",
            required = true
    )
    private String description;
    @ApiModelProperty(
            name = "deviceIdentifier",
            value = "This is a 64-bit number (as a hex string) that is randomly generated when the user first sets up the device and should remain constant for the lifetime of the user\'s device. The value may change if a factory reset is performed on the device.",
            required = true
    )
    @NotNull
    @Size(min = 2, max = 45)
    @Pattern(regexp = "^[A-Za-z0-9]*$")
    private String deviceIdentifier;
    @ApiModelProperty(
            name = "enrolmentInfo",
            value = "This defines the device registration related information. It is mandatory to define this information.",
            required = true
    )
    private EnrolmentInfo enrolmentInfo;
    @ApiModelProperty(
            name = "features",
            value = "List of features.",
            required = true
    )
    private List<Feature> features;
    private List<Device.Property> properties;
    @ApiModelProperty(
            name = "advanceInfo",
            value = "This defines the device registration related information. It is mandatory to define this information.",
            required = false
    )
    private DeviceInfo deviceInfo;
    @ApiModelProperty(
            name = "applications",
            value = "This represents the application list installed into the device",
            required = false
    )
    private List<Application> applications;

    public AndroidDevice() {
    }

    public AndroidDevice(String name, String type, String description, String deviceId, EnrolmentInfo enrolmentInfo, List<Feature> features, List<Device.Property> properties) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.deviceIdentifier = deviceId;
        this.enrolmentInfo = enrolmentInfo;
        this.features = features;
        this.properties = properties;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceIdentifier() {
        return this.deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public EnrolmentInfo getEnrolmentInfo() {
        return this.enrolmentInfo;
    }

    public void setEnrolmentInfo(EnrolmentInfo enrolmentInfo) {
        this.enrolmentInfo = enrolmentInfo;
    }

    public List<Feature> getFeatures() {
        return this.features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public List<Device.Property> getProperties() {
        return this.properties;
    }

    public void setProperties(List<Device.Property> properties) {
        this.properties = properties;
    }

    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public List<Application> getApplications() {
        return this.applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public String toString() {
        return "device [name=" + this.name + ";" + "type=" + this.type + ";" + "description=" + this.description + ";" + "identifier=" + this.deviceIdentifier + ";" + "]";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof AndroidDevice)) {
            return false;
        } else {
            AndroidDevice device = (AndroidDevice) o;
            return this.getDeviceIdentifier().equals(device.getDeviceIdentifier());
        }
    }

    public int hashCode() {
        return this.getDeviceIdentifier().hashCode();
    }

    public static class Property {
        private String name;
        private String value;

        public Property() {
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
