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

function onRequest(context) {
    var log = new Log("mdm.unit.device.operation-bar");
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var deviceType = context.uriParams.deviceType;
    var viewModel = {};
    var permissions = [];

    // permission checks
    if (deviceType == "android") {
        if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/ring")) {
            permissions.push("DEVICE_RING");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/lock-devices")) {
            permissions.push("DEVICE_LOCK");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/unlock-devices")) {
            permissions.push("DEVICE_UNLOCK");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/location")) {
            permissions.push("DEVICE_LOCATION");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/clear-password")) {
            permissions.push("CLEAR_PASSWORD");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/reboot")) {
            permissions.push("DEVICE_REBOOT");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/upgrade-firmware")) {
            permissions.push("UPGRADE_FIRMWARE");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/mute")) {
            permissions.push("DEVICE_MUTE");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/send-notification")) {
            permissions.push("NOTIFICATION");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/change-lock-code")) {
            permissions.push("CHANGE_LOCK_CODE");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/enterprise-wipe")) {
            permissions.push("ENTERPRISE_WIPE");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/wipe")) {
            permissions.push("WIPE_DATA");
        }
    } else if (deviceType == "ios") {

    } else if (deviceType == "windows") {

    }

    viewModel["permissions"] = stringify(permissions);

    return viewModel;
}