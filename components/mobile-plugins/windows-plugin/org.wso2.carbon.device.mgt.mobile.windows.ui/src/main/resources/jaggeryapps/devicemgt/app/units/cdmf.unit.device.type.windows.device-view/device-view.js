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
    // var log = new Log("device-view.js");
    var deviceType = context["uriParams"]["deviceType"];
    var deviceId = request.getParameter("id");
    var deviceViewData = {};

    if (deviceType && deviceId) {
        var deviceModule = require("/app/modules/business-controllers/device.js")["deviceModule"];
        var response = deviceModule.viewDevice(deviceType, deviceId);
        if (response["status"] == "success") {
            deviceViewData["deviceFound"] = true;
            deviceViewData["isAuthorized"] = true;

            var filteredDeviceData = response["content"];

            // creating deviceView information model from filtered device data
            var viewModel = {};
            if (filteredDeviceData["type"]) {
                viewModel["type"] = filteredDeviceData["type"];
                viewModel.isNotWindows = true;
            }
            if (filteredDeviceData["deviceIdentifier"]) {
                viewModel["deviceIdentifier"] = filteredDeviceData["deviceIdentifier"];
            }
            if (filteredDeviceData["name"]) {
                viewModel["name"] = filteredDeviceData["name"];
            }
            if (filteredDeviceData["enrolmentInfo"]) {
                if (filteredDeviceData["enrolmentInfo"]["status"]) {
                    viewModel["status"] = filteredDeviceData["enrolmentInfo"]["status"];
                    viewModel.isActive = false ;
                    viewModel.isNotRemoved = true;
                    if (filteredDeviceData["enrolmentInfo"]["status"]== "ACTIVE") {
                        viewModel.isActive = true ;
                    }
                    if (filteredDeviceData["enrolmentInfo"]["status"]== "REMOVED") {
                        viewModel.isNotRemoved = false ;
                    }
                }
                if (filteredDeviceData["enrolmentInfo"]["owner"]) {
                    viewModel["owner"] = filteredDeviceData["enrolmentInfo"]["owner"];
                }
                if (filteredDeviceData["enrolmentInfo"]["ownership"]) {
                    viewModel["ownership"] = filteredDeviceData["enrolmentInfo"]["ownership"];
                }
            }
            if (filteredDeviceData["latestDeviceInfo"]) {
                viewModel["deviceInfoAvailable"] = true;
                if (filteredDeviceData["latestDeviceInfo"]["osBuildDate"]) {
                    if (filteredDeviceData["latestDeviceInfo"]["osBuildDate"] != "0") {
                        viewModel["osBuildDate"] = new Date(filteredDeviceData["latestDeviceInfo"]["osBuildDate"] * 1000);
                    }
                }
                if (filteredDeviceData["latestDeviceInfo"]["location"]["latitude"] &&
                    filteredDeviceData["latestDeviceInfo"]["location"]["longitude"]) {
                    viewModel["location"] = {};
                    viewModel["location"]["latitude"] = filteredDeviceData["latestDeviceInfo"]["location"]["latitude"];
                    viewModel["location"]["longitude"] = filteredDeviceData["latestDeviceInfo"]["location"]["longitude"];
                    viewModel["location"]["updatedTime"] = filteredDeviceData["latestDeviceInfo"]["location"]["updatedTime"];
                }
                if (filteredDeviceData["latestDeviceInfo"]["vendor"] && filteredDeviceData["latestDeviceInfo"]["deviceModel"]) {
                    viewModel["vendor"] = filteredDeviceData["latestDeviceInfo"]["vendor"];
                    viewModel["model"] = filteredDeviceData["latestDeviceInfo"]["deviceModel"];
                }
                if (filteredDeviceData["latestDeviceInfo"]["updatedTime"]) {
                    viewModel["lastUpdatedTime"] = filteredDeviceData["latestDeviceInfo"]["updatedTime"].
                        substr(0, filteredDeviceData["latestDeviceInfo"]["updatedTime"].indexOf("+"));
                }
                viewModel["BatteryLevel"] = {};
                viewModel["BatteryLevel"]["value"] = filteredDeviceData["latestDeviceInfo"]["batteryLevel"];

                viewModel["ramUsage"] = {};
                viewModel["ramUsage"]["value"] = (filteredDeviceData["latestDeviceInfo"]["availableRAMMemory"]);

                viewModel["internalMemory"] = {};
                viewModel["internalMemory"]["value"] = replaceNaNVal(Math.round((filteredDeviceData["latestDeviceInfo"]["internalAvailableMemory"]) / 1024));

            }
            if (!filteredDeviceData["initialDeviceInfo"] && !filteredDeviceData["latestDeviceInfo"]) {
                viewModel["deviceInfoAvailable"] = false;
            }
            viewModel.locationHistory = stringify(filteredDeviceData["locationHistory"]);
            deviceViewData["device"] = viewModel;
        } else if (response["status"] == "unauthorized") {
            deviceViewData["deviceFound"] = true;
            deviceViewData["isAuthorized"] = false;
        } else if (response["status"] == "notFound") {
            deviceViewData["deviceFound"] = false;
        }
    } else {
        deviceViewData["deviceFound"] = false;
    }

    var autoCompleteParams = [
        {"name" : "deviceId", "value" : deviceId}
    ];

    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var permissions = userModule.getUIPermissions();
    deviceViewData["autoCompleteParams"] = autoCompleteParams;
    deviceViewData["permissions"] = permissions;
    return deviceViewData;

    function replaceNaNVal(val) {
        if (isNaN(val)) {
            return "N/A";
        }
        return val;
    }
}