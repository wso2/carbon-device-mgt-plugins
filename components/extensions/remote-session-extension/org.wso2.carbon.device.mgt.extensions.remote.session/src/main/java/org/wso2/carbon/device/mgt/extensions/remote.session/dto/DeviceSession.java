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
package org.wso2.carbon.device.mgt.extensions.remote.session.dto;

import org.wso2.carbon.device.mgt.extensions.remote.session.dto.common.RemoteSession;
import org.wso2.carbon.device.mgt.extensions.remote.session.internal.RemoteSessionManagementDataHolder;

import javax.websocket.Session;

/**
 * {@link DeviceSession} is the represent of device which will be connecting based on client request
 */
public class DeviceSession extends RemoteSession {


    public DeviceSession(Session session, String tenantDomain, String deviceType, String deviceId, String operationId) {
        super(session, tenantDomain, deviceType, deviceId, operationId);
    }


    @Override
    public boolean applyThrottlingPolicy() {
        if (RemoteSessionManagementDataHolder.getInstance().getMessagesPerSession() > 0) {
            long minDurationMessagesPerSecond = 1000 / RemoteSessionManagementDataHolder.getInstance()
                    .getMessagesPerSession();
            if ((System.currentTimeMillis() - getLastMessageTimeStamp()) < minDurationMessagesPerSecond) {
                return true;
            }
        }
        return false;
    }
}
