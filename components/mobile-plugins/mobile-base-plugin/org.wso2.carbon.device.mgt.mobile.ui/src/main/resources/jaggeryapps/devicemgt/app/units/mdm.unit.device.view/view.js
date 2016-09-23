/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function onRequest(context) {
    // var log = new Log("view.js");
    var deviceType = context.uriParams.deviceType;
    var deviceId = request.getParameter("id");
    var deviceData = {};

    if (deviceType && deviceId) {
        var deviceModule = require("/app/modules/business-controllers/device.js")["deviceModule"];
        var response = deviceModule.viewDevice(deviceType, deviceId);
        if (response["status"] == "success") {
            deviceData["deviceFound"] = true;
            deviceData["isAuthorized"] = true;

            var device = response["content"];
            var viewModel = {};
            var deviceInfo = device["properties"]["DEVICE_INFO"];
            if (deviceInfo && String(deviceInfo.toString()).length > 0) {
                deviceInfo = parse(stringify(deviceInfo));
                if (device["type"] == "ios") {
                    deviceInfo = parse(deviceInfo);
                    viewModel["imei"] = device["properties"]["IMEI"];
                    viewModel["phoneNumber"] = deviceInfo["PhoneNumber"];
                    viewModel["udid"] = deviceInfo["UDID"];
                    viewModel["BatteryLevel"] = Math.round(deviceInfo["BatteryLevel"] * 100);
                    viewModel["DeviceCapacity"] = Math.round(deviceInfo["DeviceCapacity"] * 100) / 100;
                    viewModel["AvailableDeviceCapacity"] = Math.
                        round(deviceInfo["AvailableDeviceCapacity"] * 100) / 100;
                    viewModel["DeviceCapacityUsed"] = Math.
                        round((viewModel["DeviceCapacity"] - viewModel["AvailableDeviceCapacity"]) * 100) / 100;
                    viewModel["DeviceCapacityPercentage"] = Math.
                        round(viewModel["AvailableDeviceCapacity"] / viewModel["DeviceCapacity"] * 10000) / 100;
                    viewModel["location"] = {
                        latitude: device["properties"]["LATITUDE"],
                        longitude: device["properties"]["LONGITUDE"]
                    };
                } else if (device["type"] == "android") {
                    viewModel["deviceName"] = device["name"];
                    viewModel["deviceIdentifier"] = device["deviceIdentifier"];
                    viewModel["imei"] = device["properties"]["IMEI"];
                    viewModel["model"] = device["deviceInfo"]["deviceModel"];
                    viewModel["vendor"] = device["deviceInfo"]["vendor"];
                    viewModel["owner"] = device["owner"];
                    viewModel["ownership"] = device["ownership"];
                    viewModel["lastUpdatedTime"] = device["deviceInfo"]["updatedTime"].
                        substr(0, device["deviceInfo"]["updatedTime"].indexOf("+"));

                    var osBuildDate = device["properties"]["OS_BUILD_DATE"];
                    if (osBuildDate != null && osBuildDate != "0") {
                        viewModel["os_build_date"] = new Date(osBuildDate * 1000);
                    }

                    viewModel["location"] = {
                        latitude: device["properties"]["LATITUDE"],
                        longitude: device["properties"]["LONGITUDE"]
                    };
                    var info = {};
                    var infoList = parse(deviceInfo);
                    if (infoList != null && infoList != undefined) {
                        for (var j = 0; j < infoList.length; j++) {
                            info[infoList[j].name] = infoList[j].value;
                        }
                    }
                    deviceInfo = info;
                    viewModel["BatteryLevel"] = {};
                    viewModel["BatteryLevel"]["value"] = device["deviceInfo"]["batteryLevel"];

                    viewModel["cpuUsage"] = {};
                    viewModel["cpuUsage"]["value"] = device["deviceInfo"]["cpuUsage"];

                    viewModel["ramUsage"] = {};
                    if (device["deviceInfo"]["totalRAMMemory"] != 0) {
                        viewModel["ramUsage"]["value"] = Math.
                            round((device["deviceInfo"]["totalRAMMemory"] - device["deviceInfo"]["availableRAMMemory"])
                                / device["deviceInfo"]["totalRAMMemory"] * 10000) / 100;
                    } else {
                        viewModel["ramUsage"]["value"] = 0;
                    }

                    viewModel["internalMemory"] = {};
                    viewModel["externalMemory"] = {};
                    viewModel["internalMemory"]["total"] = Math.
                        round(device["deviceInfo"]["internalTotalMemory"] * 100) / 100;
                    if (device["deviceInfo"]["internalTotalMemory"] != 0) {
                        viewModel["internalMemory"]["usage"] = Math.
                            round((device["deviceInfo"]["internalTotalMemory"] - device["deviceInfo"]["internalAvailableMemory"])
                                / device["deviceInfo"]["internalTotalMemory"] * 10000) / 100;
                    } else {
                        viewModel["internalMemory"]["usage"] = 0;
                    }

                    viewModel["externalMemory"]["total"] = Math.
                        round(device["deviceInfo"]["externalTotalMemory"] * 100) / 100;
                    if (device["deviceInfo"]["externalTotalMemory"] != 0) {
                        viewModel["externalMemory"]["usage"] = Math.
                            round((device["deviceInfo"]["externalTotalMemory"] - device["deviceInfo"]["externalAvailableMemory"])
                                / device["deviceInfo"]["externalTotalMemory"] * 10000) / 100;
                    } else {
                        viewModel["externalMemory"]["usage"] = 0;
                    }
                } else if (device["type"] == "windows") {
                    viewModel["imei"] = device["properties"]["IMEI"];
                    viewModel["model"] = device["properties"]["DEVICE_MODEL"];
                    viewModel["vendor"] = device["properties"]["VENDOR"];
                    viewModel["internalMemory"] = {};
                    viewModel["externalMemory"] = {};
                    viewModel["location"] = {
                        latitude: device["properties"]["LATITUDE"],
                        longitude: device["properties"]["LONGITUDE"]
                    };
                }
                device["viewModel"] = viewModel;
            }
            deviceData["device"] = device;
        } else if (response["status"] == "unauthorized") {
            deviceData["deviceFound"] = true;
            deviceData["isAuthorized"] = false;
        } else if (response["status"] == "notFound") {
            deviceData["deviceFound"] = false;
        }
        return deviceData;
    }
}