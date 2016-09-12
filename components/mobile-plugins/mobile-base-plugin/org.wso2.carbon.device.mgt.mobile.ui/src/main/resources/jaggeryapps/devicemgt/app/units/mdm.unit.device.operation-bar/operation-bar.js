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
    // var log = new Log("mdm.unit.device.operation-bar");
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var viewModel = {};
    var permissions = {};

    // adding android operations related permission checks
    permissions["android"] = [];
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/ring")) {
        permissions["android"].push("DEVICE_RING");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/lock-devices")) {
        permissions["android"].push("DEVICE_LOCK");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/unlock-devices")) {
        permissions["android"].push("DEVICE_UNLOCK");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/location")) {
        permissions["android"].push("DEVICE_LOCATION");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/clear-password")) {
        permissions["android"].push("CLEAR_PASSWORD");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/reboot")) {
        permissions["android"].push("DEVICE_REBOOT");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/upgrade-firmware")) {
        permissions["android"].push("UPGRADE_FIRMWARE");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/mute")) {
        permissions["android"].push("DEVICE_MUTE");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/send-notification")) {
        permissions["android"].push("NOTIFICATION");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/change-lock-code")) {
        permissions["android"].push("CHANGE_LOCK_CODE");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/enterprise-wipe")) {
        permissions["android"].push("ENTERPRISE_WIPE");
    }
    if (userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/operations/android/wipe")) {
        permissions["android"].push("WIPE_DATA");
    }

    // adding ios operations related permission checks

    // adding windows operations related permission checks

    viewModel["permissions"] = stringify(permissions);

    return viewModel;
}