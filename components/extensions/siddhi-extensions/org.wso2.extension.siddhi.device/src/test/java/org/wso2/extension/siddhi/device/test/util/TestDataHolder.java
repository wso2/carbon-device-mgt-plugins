/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.extension.siddhi.device.test.util;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class TestDataHolder {

    public final static String initialDeviceIdentifier = "12345";
    public final static String OWNER = "admin";

    public static Device generateDummyDeviceData(String deviceType) {
        Device device = new Device();
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setDateOfEnrolment(new Date().getTime());
        enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
        enrolmentInfo.setOwner(OWNER);
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
        device.setEnrolmentInfo(enrolmentInfo);
        device.setDescription("Test Description");
        device.setDeviceIdentifier(initialDeviceIdentifier);
        device.setType(deviceType);
        return device;
    }

    public static DeviceGroup generateDummyGroupData(int testId) {
        DeviceGroup deviceGroup = new DeviceGroup();
        deviceGroup.setName("Test" + testId);
        deviceGroup.setDescription("Test description " + testId);
        deviceGroup.setOwner(OWNER);
        return deviceGroup;
    }

    public static List<DeviceIdentifier> getDeviceIdentifiersList(String deviceType){
        Device device = generateDummyDeviceData(deviceType);
        DeviceIdentifier identifier = new DeviceIdentifier();
        identifier.setId(device.getDeviceIdentifier());
        identifier.setType(deviceType);

        List<DeviceIdentifier> list = new ArrayList<>();
        list.add(identifier);

        return list;
    }
}
