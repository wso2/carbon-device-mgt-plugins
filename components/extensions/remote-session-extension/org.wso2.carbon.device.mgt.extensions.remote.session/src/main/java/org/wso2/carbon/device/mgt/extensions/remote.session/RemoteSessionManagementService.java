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
package org.wso2.carbon.device.mgt.extensions.remote.session;

import org.wso2.carbon.device.mgt.extensions.remote.session.exception.RemoteSessionManagementException;

import javax.websocket.Session;
import java.io.IOException;

/**
 * Class @{@link RemoteSessionManagementService} use for managing remote sessions
 */
public interface RemoteSessionManagementService {

    /**
     * Initialize session based on web socket request .This method is used by the device to connect
     *
     * @param session    Web socket RemoteSession
     * @param deviceType Device Type
     * @param deviceId   Device Id
     * @throws RemoteSessionManagementException throws when session has errors with accessing device resources
     */
    void initializeSession(Session session, String deviceType, String deviceId) throws
            RemoteSessionManagementException;

    /**
     * Initialize session based on web socket request . This method is used by the device to connect
     *
     * @param session     Web socket RemoteSession
     * @param deviceType  Device Type
     * @param deviceId    Device Id
     * @param operationId Operation Id that device needs to connec
     * @throws RemoteSessionManagementException throws when session has errors with accessing device resources
     */
    void initializeSession(Session session, String deviceType, String deviceId, String operationId) throws
            RemoteSessionManagementException;

    /**
     * Send string message to connected remote device or client
     *
     * @param session Web socket RemoteSession
     * @param message Message needs to send to peer connection client
     * @throws RemoteSessionManagementException
     */
    void sendMessageToPeer(Session session, String message) throws  RemoteSessionManagementException;

    /**
     * Send byte message to connected remote device or client
     *
     * @param session Web socket RemoteSession
     * @param message Message needs to send to peer connection
     * @throws RemoteSessionManagementException
     */
    void sendMessageToPeer(Session session, byte[] message) throws RemoteSessionManagementException;

    /**
     * Close the session
     *
     * @param session Web socket RemoteSession
     */
    void endSession(Session session, String closeReason);

}
