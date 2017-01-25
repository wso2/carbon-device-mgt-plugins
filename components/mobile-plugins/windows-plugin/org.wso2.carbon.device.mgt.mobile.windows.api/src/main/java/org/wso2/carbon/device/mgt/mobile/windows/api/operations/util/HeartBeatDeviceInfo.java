/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.mobile.windows.api.operations.util;

import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.PluginConstants;

import java.util.ArrayList;
import java.util.List;

public class HeartBeatDeviceInfo {
    public List<Operation> getDeviceInfo() {

        List<Operation> deviceInfoOperations = new ArrayList<>();

        Operation osVersion = new Operation();
        osVersion.setCode(PluginConstants.SyncML.SOFTWARE_VERSION);
        deviceInfoOperations.add(osVersion);

        Operation imsi = new Operation();
        imsi.setCode(PluginConstants.SyncML.IMSI);
        deviceInfoOperations.add(imsi);

        Operation imei = new Operation();
        imei.setCode(PluginConstants.SyncML.IMEI);
        deviceInfoOperations.add(imei);

        Operation deviceID = new Operation();
        deviceID.setCode(PluginConstants.SyncML.DEV_ID);
        deviceInfoOperations.add(deviceID);

        Operation manufacturer = new Operation();
        manufacturer.setCode(PluginConstants.SyncML.MANUFACTURER);
        deviceInfoOperations.add(manufacturer);

        Operation model = new Operation();
        model.setCode(PluginConstants.SyncML.MODEL);
        deviceInfoOperations.add(model);

        Operation language = new Operation();
        language.setCode(PluginConstants.SyncML.LANGUAGE);
        deviceInfoOperations.add(language);

        Operation vendor = new Operation();
        vendor.setCode(PluginConstants.SyncML.VENDOR);
        deviceInfoOperations.add(vendor);

        Operation macaddress = new Operation();
        macaddress.setCode(PluginConstants.SyncML.MAC_ADDRESS);
        deviceInfoOperations.add(macaddress);

        Operation resolution = new Operation();
        resolution.setCode(PluginConstants.SyncML.RESOLUTION);
        deviceInfoOperations.add(resolution);

        Operation deviceName = new Operation();
        deviceName.setCode(PluginConstants.SyncML.DEVICE_NAME);
        deviceInfoOperations.add(deviceName);

        Operation totalRam = new Operation();
        totalRam.setCode(PluginConstants.SyncML.TOTAL_RAM);
        deviceInfoOperations.add(totalRam);

        Operation availableStorage = new Operation();
        availableStorage.setCode(PluginConstants.SyncML.TOTAL_STORAGE);
        deviceInfoOperations.add(availableStorage);

        Operation remainingBattery = new Operation();
        remainingBattery.setCode(PluginConstants.SyncML.BATTERY_CHARGE_REMAINING);
        deviceInfoOperations.add(remainingBattery);

        return deviceInfoOperations;
    }
}
