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
package org.wso2.carbon.device.mgt.iot.output.adapter.ui.authorization;

import org.wso2.carbon.device.mgt.iot.output.adapter.ui.authentication.AuthenticationInfo;
import org.wso2.carbon.device.mgt.iot.output.adapter.ui.constants.WebsocketConstants;
import org.wso2.carbon.device.mgt.iot.output.adapter.ui.util.WebSocketSessionRequest;

import javax.websocket.Session;
import java.util.Map;

/**
 * This authorizer crossvalidates the request with device id and device type.
 */
public class DeviceAuthorizer implements Authorizer {
    private static final String STATS_SCOPE_IDENTIFIER = "stats";
    private static final String DEVICE_MGT_SCOPE_IDENTIFIER = "device-mgt";

    @Override
    public boolean isAuthorized(AuthenticationInfo authenticationInfo, Session session, String stream) {
        WebSocketSessionRequest webSocketSessionRequest = new WebSocketSessionRequest(session);
        Map<String, String> queryParams = webSocketSessionRequest.getQueryParamValuePairs();
        String deviceId = queryParams.get("deviceId");
        String deviceType = queryParams.get("deviceType");
        Object scopeObject = authenticationInfo.getProperties().get(WebsocketConstants.SCOPE_IDENTIFIER);

        if (deviceId != null && !deviceId.isEmpty() && deviceType != null && !deviceType.isEmpty()
                && scopeObject != null) {
            String scopes[] = (String[]) scopeObject;
            String requiredScope = DEVICE_MGT_SCOPE_IDENTIFIER + ":" + deviceType + ":" + deviceId + ":"
                    + STATS_SCOPE_IDENTIFIER;
            for (String scope : scopes) {
                if (requiredScope.equals(scope)) {
                    return true;
                }
            }
        }
        return false;
    }
}