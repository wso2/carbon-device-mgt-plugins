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
package org.wso2.carbon.device.mgt.extensions.remote.session.dto.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.extensions.remote.session.constants.RemoteSessionConstants;
import org.wso2.carbon.device.mgt.extensions.remote.session.exception.RemoteSessionInvalidException;
import org.wso2.carbon.device.mgt.extensions.remote.session.exception.RemoteSessionManagementException;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * {@link RemoteSession} will represent remote websocket session
 *  This class implements the behaviours of sending message to the session in multithreaded environment.
 *
 */
public abstract class RemoteSession {

    private static final Log log = LogFactory.getLog(RemoteSession.class);
    private String tenantDomain, operationId, deviceType, deviceId;
    private long lastMessageTimeStamp = System.currentTimeMillis();
    ;
    private RemoteSession peerSession;
    private Session mySession;
    private final Object writeLockObject = new Object();

    protected RemoteSession(Session session, String tenantDomain, String deviceType, String deviceId, String
            operationId) {
        this.mySession = session;
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        this.tenantDomain = tenantDomain;
        this.operationId = operationId;
    }

    public void sendMessage(Object message) throws RemoteSessionInvalidException, RemoteSessionManagementException {

        if (message != null) {
            boolean isMessageCountExceed = false;
            if (mySession != null && mySession.isOpen()) {
                synchronized (writeLockObject) {
                    try {
                        isMessageCountExceed = applyThrottlingPolicy();
                        if (!isMessageCountExceed) {
                            if (message instanceof String) {
                                mySession.getBasicRemote().sendText(message.toString());
                            } else {
                                mySession.getBasicRemote().sendBinary(ByteBuffer.wrap((byte[]) message));
                            }
                            this.lastMessageTimeStamp = System.currentTimeMillis();
                        }
                    } catch (IOException e) {
                        log.warn("Send data to session failed due to ", e);
                    }
                }
            } else {
                throw new RemoteSessionInvalidException("Peer Session already closed ", new CloseReason
                        (CloseReason.CloseCodes.CANNOT_ACCEPT, "Peer Session already closed "));
            }

            if (isMessageCountExceed) {
                JSONObject response = new JSONObject();
                response.put("code", RemoteSessionConstants.THROTTLE_OUT);
                sendMessageToPeer(message.toString());
            }
        } else {
            throw new RemoteSessionManagementException("Message is empty");
        }
    }

    public void sendMessageToPeer(Object message) throws RemoteSessionInvalidException, RemoteSessionManagementException {
        peerSession.sendMessage(message);
    }


    public abstract boolean applyThrottlingPolicy();


    public Session getMySession() {
        return mySession;
    }

    public void setMySession(Session mySession) {
        this.mySession = mySession;
    }

    public RemoteSession getPeerSession() {
        return peerSession;
    }

    public void setPeerSession(RemoteSession peerSession) {
        this.peerSession = peerSession;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getLastMessageTimeStamp() {
        return lastMessageTimeStamp;
    }

    public void setLastMessageTimeStamp(long lastMessageTimeStamp) {
        this.lastMessageTimeStamp = lastMessageTimeStamp;
    }
}
