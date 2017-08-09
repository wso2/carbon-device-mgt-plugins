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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.extensions.remote.session.constants.RemoteSessionConstants;
import org.wso2.carbon.device.mgt.extensions.remote.session.exception.RemoteSessionInvalidException;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;

public class RemoteSession {

    private static final Log log = LogFactory.getLog(RemoteSession.class);
    private String tenantDomain, operationId, deviceType, deviceId;
    private Session peerSession;
    private RemoteSessionConstants.CONNECTION_MODES connection_mode;
    private final Object writeLockObject = new Object();


    public RemoteSession(String tenantDomain, String deviceType, String deviceId, String operationId, RemoteSessionConstants.CONNECTION_MODES connection_mode) {
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        this.tenantDomain = tenantDomain;
        this.operationId = operationId;
        this.connection_mode = connection_mode;
    }

    public void sendMessageToPeer(String message) throws RemoteSessionInvalidException {
        if (peerSession != null && peerSession.isOpen()) {
            synchronized (writeLockObject) {
                try {
                    peerSession.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.warn("Send data to session failed due to ", e);
                }
            }
        } else {
            throw new RemoteSessionInvalidException("Peer Session already closed ", new CloseReason
                    (CloseReason.CloseCodes.CANNOT_ACCEPT, "Peer Session already closed "));
        }
    }

    public void sendMessageToPeer(byte[] message) throws RemoteSessionInvalidException {

        if (peerSession != null && peerSession.isOpen()) {
            synchronized (writeLockObject) {
                try {
                    peerSession.getBasicRemote().sendBinary(ByteBuffer.wrap(message));
                } catch (IOException e) {
                    log.warn("Send data to session failed due to ", e);
                }
            }
        } else {
            throw new RemoteSessionInvalidException("Peer Session already closed ", new CloseReason
                    (CloseReason.CloseCodes.CANNOT_ACCEPT, "Peer Session already closed "));
        }
    }

    public Session getPeerSession() {
        return peerSession;
    }

    public void setPeerSession(Session peerSession) {
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

    public RemoteSessionConstants.CONNECTION_MODES getConnection_mode() {
        return connection_mode;
    }

    public void setConnection_mode(RemoteSessionConstants.CONNECTION_MODES connection_mode) {
        this.connection_mode = connection_mode;
    }
}
