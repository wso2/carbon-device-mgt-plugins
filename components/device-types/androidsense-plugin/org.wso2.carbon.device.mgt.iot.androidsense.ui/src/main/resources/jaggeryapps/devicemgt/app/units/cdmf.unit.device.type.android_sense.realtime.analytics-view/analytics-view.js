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
    var log = new Log("stats.js");
    var carbonServer = require("carbon").server;
    var device = context.unit.params.device;
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];
    var constants = require("/app/modules/constants.js");

    var websocketEndpoint = devicemgtProps["wssURL"].replace("https", "wss");
	var tokenPair = session.get(constants["TOKEN_PAIR"]);
	if (tokenPair) {
		var token = parse(tokenPair)["accessToken"];
		websocketEndpoint = websocketEndpoint + "/secured-websocket/org.wso2.iot.android.sense/1.0.0?" +
			"token="+ token +"&deviceId=" + device.deviceIdentifier + "&deviceType=" + device.type;
	}
    return {"device": device, "websocketEndpoint": websocketEndpoint};
}