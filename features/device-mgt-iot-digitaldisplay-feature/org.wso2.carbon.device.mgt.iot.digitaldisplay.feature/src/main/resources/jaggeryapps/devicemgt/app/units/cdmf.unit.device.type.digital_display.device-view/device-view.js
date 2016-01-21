/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

    var log = new Log("device-view.js");
    var deviceType = context.uriParams.deviceType;
    var deviceId = request.getParameter("id");
    var owner = context.user.username;

    var TokenClient = Packages.org.wso2.carbon.device.mgt.iot.apimgt.TokenClient;
    var accessTokenClient = new TokenClient(deviceType);
    var accessTokenInfo = accessTokenClient.getAccessToken(owner, deviceId);
    var accessToken =  accessTokenInfo.getAccess_token();

    var getProperty = require("process").getProperty;
    var port = getProperty("carbon.https.port");
    var host = getProperty("carbon.local.ip");

    if (deviceType != null && deviceType != undefined && deviceId != null && deviceId != undefined) {
        var deviceModule = require("/app/modules/device.js").deviceModule;
        var device = deviceModule.viewDevice(deviceType, deviceId);

        if (device && device.status != "error") {
            log.info(device);
            return {"device": device, "token" : accessToken , "port" : port, "host" : host};
        }
    }
}