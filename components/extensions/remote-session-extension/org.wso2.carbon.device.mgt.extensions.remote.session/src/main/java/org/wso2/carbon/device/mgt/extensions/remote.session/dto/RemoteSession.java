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
import org.wso2.carbon.device.mgt.extensions.remote.session.exception.RemoteSessionManagementException;
import org.wso2.carbon.device.mgt.extensions.remote.session.internal.RemoteSessionManagementDataHolder;

import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * {@link RemoteSession} will represent remote websocket session
 * This class implements the behaviours of sending message to the session in multithreaded context.
 */
public class RemoteSession {

    private static final Log log = LogFactory.getLog(RemoteSession.class);
    private String tenantDomain, operationId, deviceType, deviceId;
    private long lastMessageTimeStamp = System.currentTimeMillis();
    private RemoteSession peerSession;
    private Session mySession;
    private final Object writeLockObject = new Object();
    private int maxMessagesPerSecond;
    private int messageAllowance;
    private double messageRatePerSecond;

    public RemoteSession(Session session, String tenantDomain, String deviceType, String deviceId) {
        this.mySession = session;
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        this.tenantDomain = tenantDomain;
        maxMessagesPerSecond = RemoteSessionManagementDataHolder.getInstance().getMaxMessagesPerSecond();
        messageAllowance = maxMessagesPerSecond;
        messageRatePerSecond = (double) maxMessagesPerSecond / 1000;
    }

    private void sendMessage(Object message) throws RemoteSessionManagementException {
        if (message != null) {
            boolean isMessageCountExceed = false;
            if (mySession != null && mySession.isOpen()) {
                synchronized (writeLockObject) {
                    try {
                        isMessageCountExceed = applyRateLimit();
                        if (!isMessageCountExceed) {
                            if (message instanceof String) {
                                mySession.getBasicRemote().sendText(message.toString());
                            } else {
                                mySession.getBasicRemote().sendBinary(ByteBuffer.wrap((byte[]) message));
                            }
                            this.lastMessageTimeStamp = System.currentTimeMillis();
                        } else {
                            log.warn("Message count per second is exceeded for device id :" + deviceId);
                        }
                    } catch (IOException e) {
                        log.warn("Send data to session failed due to ", e);
                    }
                }
            } else {
                throw new RemoteSessionManagementException("Peer Session already closed ");
            }
        } else {
            throw new RemoteSessionManagementException("Message is empty");
        }
    }

    public void sendMessageToPeer(Object message) throws RemoteSessionManagementException {
        peerSession.sendMessage(message);
    }

    /**
     * Use for limit the messages for given time
     *
     * @return message rate applied
     */
    private boolean applyRateLimit() {
        long currentTime = System.currentTimeMillis();
        messageAllowance += (currentTime - lastMessageTimeStamp) * messageRatePerSecond;
        if (messageAllowance > maxMessagesPerSecond) {
            messageAllowance = maxMessagesPerSecond;
        }
        if (messageAllowance >= 1) {
            lastMessageTimeStamp = currentTime;
            messageAllowance -= 1;
            return false;
        } else {
            return true;
        }
    }

    public Session getMySession() {
        return mySession;
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

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
