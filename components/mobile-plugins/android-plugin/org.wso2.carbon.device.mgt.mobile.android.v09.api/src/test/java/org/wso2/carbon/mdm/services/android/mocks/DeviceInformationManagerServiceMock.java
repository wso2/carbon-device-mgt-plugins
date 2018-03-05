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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.services.android.mocks;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;

import java.util.List;

public class DeviceInformationManagerServiceMock implements DeviceInformationManager {
    @Override
    public void addDeviceInfo(DeviceIdentifier deviceIdentifier, DeviceInfo deviceInfo)
            throws DeviceDetailsMgtException {

    }

    @Override
    public DeviceInfo getDeviceInfo(DeviceIdentifier deviceIdentifier) throws DeviceDetailsMgtException {
        return null;
    }

    @Override
    public List<DeviceInfo> getDevicesInfo(List<DeviceIdentifier> list) throws DeviceDetailsMgtException {
        return null;
    }

    @Override
    public void addDeviceLocation(DeviceLocation deviceLocation) throws DeviceDetailsMgtException {

    }

    @Override
    public DeviceLocation getDeviceLocation(DeviceIdentifier deviceIdentifier) throws DeviceDetailsMgtException {
        return null;
    }

    @Override
    public List<DeviceLocation> getDeviceLocations(List<DeviceIdentifier> list) throws DeviceDetailsMgtException {
        return null;
    }
}
