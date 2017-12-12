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
package org.wso2.carbon.mdm.services.android.bean.wrapper;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;

import java.io.Serializable;

@ApiModel(value = "EnrolmentInfo", description = "This class carries all information related to a devices enrollment" +
        " status.")
public class DeviceEnrollmentInfo extends EnrolmentInfo implements Serializable  {
    private static final long serialVersionUID = 1998101712L;

    @ApiModelProperty(name = "device", value = "Enrolled device.", required = true)
    private Device device;
    @ApiModelProperty(name = "dateOfEnrolment", value = "Date of the device enrollment.", required = true)
    private Long dateOfEnrolment;
    @ApiModelProperty(name = "dateOfLastUpdate", value = "Date of the device's last update.", required = true)
    private Long dateOfLastUpdate;
    @ApiModelProperty(name = "ownership", value = "Defines the ownership details. The ownership type can be any of the" +
            " following values.\n" +
            "BYOD - Bring your own device (BYOD).\n" +
            "COPE - Corporate owned personally enabled (COPE).", required = true)
    private OwnerShip ownership;
    @ApiModelProperty(name = "status", value = "Current status of the device, such as whether the device " +
            "is active, removed etc.", required = true)
    private Status status;
    @ApiModelProperty(name = "owner", value = "The device owner's name.", required = true)
    private String owner;

    public DeviceEnrollmentInfo() {
    }

    public DeviceEnrollmentInfo(Device device, String owner, OwnerShip ownership, Status status) {
        this.device = device;
        this.owner = owner;
        this.ownership = ownership;
        this.status = status;
    }

    public Long getDateOfEnrolment() {
        return dateOfEnrolment;
    }

    public void setDateOfEnrolment(Long dateOfEnrolment) {
        this.dateOfEnrolment = dateOfEnrolment;
    }

    public Long getDateOfLastUpdate() {
        return dateOfLastUpdate;
    }

    public void setDateOfLastUpdate(Long dateOfLastUpdate) {
        this.dateOfLastUpdate = dateOfLastUpdate;
    }

    public OwnerShip getOwnership() {
        return ownership;
    }

    public void setOwnership(OwnerShip ownership) {
        this.ownership = ownership;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeviceEnrollmentInfo) {
            DeviceEnrollmentInfo tempInfo = (DeviceEnrollmentInfo) obj;
            if (this.owner != null && this.ownership != null) {
                if (this.owner.equals(tempInfo.getOwner()) && this.ownership.equals(tempInfo.getOwnership())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return owner.hashCode() ^ ownership.hashCode();
    }


}