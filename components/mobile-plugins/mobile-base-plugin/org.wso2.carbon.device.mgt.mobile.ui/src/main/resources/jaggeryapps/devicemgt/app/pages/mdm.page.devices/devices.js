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

function onRequest() {
    var userModule = require("/app/modules/business-controllers/user.js")["userModule"];
    var constants = require("/app/modules/constants.js");
    var viewModel = {};
    var permissions = {};

    // permission checks
    // [1] checking enrollment permissions
    permissions["ENROLL_DEVICE"] = userModule.isAuthorized("/permission/admin/device-mgt/devices/enroll/android") ||
        userModule.isAuthorized("/permission/admin/device-mgt/devices/enroll/ios") ||
        userModule.isAuthorized("/permission/admin/device-mgt/devices/enroll/windows");

    // [2] checking advanced device search permissions
    permissions["ADVANCED_SEARCH"] = userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/view");

    // [3] checking device viewing permission
    permissions["VIEW_DEVICES"] = userModule.isAuthorized("/permission/admin/device-mgt/devices/owning/view");

    var currentUser = session.get(constants["USER_SESSION_KEY"]);

    viewModel["permissions"] = permissions;
    viewModel["currentUser"] = currentUser;

    return viewModel;
}