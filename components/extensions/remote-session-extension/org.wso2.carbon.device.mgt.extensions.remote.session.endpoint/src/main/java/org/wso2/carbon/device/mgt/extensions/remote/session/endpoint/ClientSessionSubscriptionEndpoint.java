/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * deviceId 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.device.mgt.extensions.remote.session.endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.extensions.remote.session.constants.RemoteSessionConstants;
import org.wso2.carbon.device.mgt.extensions.remote.session.exception.RemoteSessionInvalidException;
import org.wso2.carbon.device.mgt.extensions.remote.session.exception.RemoteSessionManagementException;
import org.wso2.carbon.device.mgt.extensions.remote.session.endpoint.utils.HttpSessionConfigurator;
import org.wso2.carbon.device.mgt.extensions.remote.session.endpoint.utils.ServiceHolder;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * This class represents web socket endpoint to manage Remote Sessions
 */
@ServerEndpoint(value = "/clients/{deviceType}/{deviceId}", configurator = HttpSessionConfigurator.class)
public class ClientSessionSubscriptionEndpoint extends SubscriptionEndpoint {

    private static final Log log = LogFactory.getLog(ClientSessionSubscriptionEndpoint.class);
    private RemoteSessionConstants.CONNECTION_MODES connectionMode = RemoteSessionConstants.CONNECTION_MODES.CLIENT_MODE;

    /**
     * Web socket onOpen - When client sends a message
     *
     * @param session - Users registered session..
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("deviceType") String deviceType, @PathParam("deviceId") String
            deviceId) {
        System.out.print("**************Open***************");
        if (log.isDebugEnabled()) {
            log.debug("WebSocket opened, for RemoteSession id: " + session.getId());
        }
        try {
            ServiceHolder.getInstance().getRemoteSessionManagementService().initializeSession(session, deviceType, deviceId);
            System.out.print("**************Opened***************");
        } catch (RemoteSessionInvalidException e) {
            System.out.print(e.getMessage());
            if (log.isDebugEnabled()) {
                log.error("Error occurred while initializing session ", e);
            }
            try {
                session.close(e.getCloseReason());
            } catch (IOException ex) {
                log.error("Failed to disconnect the client.", ex);
            }
        } catch (RemoteSessionManagementException e) {
            System.out.print(e.getMessage());
            if (log.isDebugEnabled()) {
                log.error("Error occurred while initializing session ", e);
            }
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Error occurred while adding operation"));
            } catch (IOException ex) {
                if (log.isDebugEnabled()) {
                    log.error("Failed to disconnect the client.", ex);
                }
            }
        }

    }

    /**
     * Web socket onMessage - When client sends a message
     *
     * @param session - Users registered session.
     * @param message - Status code for web-socket close.
     */
    @OnMessage
    public void onMessage(Session session, String message, @PathParam("deviceType") String deviceType, @PathParam
            ("deviceId") String deviceId) throws RemoteSessionManagementException {
        super.onMessage(session, message, deviceType, deviceId);
    }

    /**
     * Web socket onMessage - When client sends a message
     *
     * @param session - Users registered session.
     * @param message - Message which needs to send to peer
     */
    @OnMessage
    public void onMessage(Session session, byte[] message, @PathParam("deviceType") String deviceType, @PathParam
            ("deviceId") String deviceId) throws RemoteSessionManagementException {
        super.onMessage(session, message, deviceType, deviceId);
    }

    /**
     * Web socket onClose - Remove the registered sessions
     *
     * @param session - Users registered session.
     * @param reason  - Status code for web-socket close.
     */
    @OnClose
    public void onClose(Session session, CloseReason reason, @PathParam("deviceType") String deviceType, @PathParam
            ("deviceId") String deviceId) {
        super.onClose(session, reason, deviceType, deviceId);
    }

    /**
     * Web socket onError - Remove the registered sessions
     *
     * @param session   - Users registered session.
     * @param throwable - Status code for web-socket close.
     */
    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("deviceType") String deviceType, @PathParam
            ("deviceId") String deviceId) {
        super.onError(session, throwable, deviceType, deviceId);
    }
}