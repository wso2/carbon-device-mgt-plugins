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

import org.wso2.carbon.device.mgt.extensions.remote.session.exception.RemoteSessionInvalidException;
import org.wso2.carbon.device.mgt.extensions.remote.session.exception.RemoteSessionManagementException;

import javax.websocket.Session;
import java.io.IOException;

/**
 * Class @{@link RemoteSessionManagementService} use for managing remote sessions
 */
public interface RemoteSessionManagementService {

    /**
     * Initialize session based on web socket request .this method use by the device to connect
     *
     * @param session    Web socket RemoteSession
     * @param deviceType Device Type
     * @param deviceId   Device Id
     * @throws RemoteSessionInvalidException    throws when session cannot be made due to invalid data
     * @throws RemoteSessionManagementException throws when session has error with accessing device resources
     */
    public void initializeSession(Session session, String deviceType, String deviceId) throws
            RemoteSessionInvalidException, RemoteSessionManagementException;

    /**
     * Initialize session based on web socket request . This method use by the device to connect
     *
     * @param session     Web socket RemoteSession
     * @param deviceType  Device Type
     * @param deviceId    Device Id
     * @param operationId Operation Id that device needs to connec
     * @throws RemoteSessionInvalidException    throws when session cannot be made due to invalid data
     * @throws RemoteSessionManagementException throws when session has error with accessing device resources
     */
    public void initializeSession(Session session, String deviceType, String deviceId, String operationId) throws
            RemoteSessionInvalidException, RemoteSessionManagementException;

    /**
     * Send message to connected remote device or client
     *
     * @param session Web socket RemoteSession
     * @param message Message needs to send to peer connection client
     * @throws RemoteSessionInvalidException
     * @throws RemoteSessionManagementException
     */
    public void sendMessageToPeer(Session session, String message) throws
            RemoteSessionInvalidException, RemoteSessionManagementException;

    /**
     * Send message to connected remote device or client
     *
     * @param session Web socket RemoteSession
     * @param message Message needs to send to peer connection
     * @throws RemoteSessionInvalidException
     * @throws RemoteSessionManagementException
     */
    public void sendMessageToPeer(Session session, byte[] message) throws
            RemoteSessionInvalidException, RemoteSessionManagementException;

    /**
     * Close the session
     *
     * @param session Web socket RemoteSession
     */
    public void endSession(Session session) throws IOException;

}
