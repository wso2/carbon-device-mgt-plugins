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

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.operation.mgt.ConfigOperation;
import org.wso2.carbon.device.mgt.extensions.remote.session.authentication.AuthenticationInfo;
import org.wso2.carbon.device.mgt.extensions.remote.session.authentication.OAuthAuthenticator;
import org.wso2.carbon.device.mgt.extensions.remote.session.constants.RemoteSessionConstants;
import org.wso2.carbon.device.mgt.extensions.remote.session.dto.RemoteSession;
import org.wso2.carbon.device.mgt.extensions.remote.session.exception.RemoteSessionManagementException;
import org.wso2.carbon.device.mgt.extensions.remote.session.internal.RemoteSessionManagementDataHolder;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RemoteSessionManagementServiceImpl implements RemoteSessionManagementService {

    private static final Log log = LogFactory.getLog(RemoteSessionManagementServiceImpl.class);

    @Override
    public void initializeSession(Session session, String deviceType, String deviceId, String operationId) throws
            RemoteSessionManagementException {

        // Check whether required configurations are enabled
        if (!RemoteSessionManagementDataHolder.getInstance().isEnabled()) {
            throw new RemoteSessionManagementException("Remote session feature is disabled.");
        } else if (RemoteSessionManagementDataHolder.getInstance().getServerUrl() == null) {
            throw new RemoteSessionManagementException("Server url has not been configured.");
        }

        // Read Query Parameters for obtain the token
        Map<String, List<String>> sessionQueryParam = new HashedMap();
        List<String> sessionQueryParamList = new LinkedList<>();
        sessionQueryParamList.add(session.getQueryString());
        sessionQueryParam.put(RemoteSessionConstants.QUERY_STRING, sessionQueryParamList);

        // Validate the token
        OAuthAuthenticator oAuthAuthenticator = RemoteSessionManagementDataHolder.getInstance().getOauthAuthenticator();
        AuthenticationInfo authenticationInfo = oAuthAuthenticator.isAuthenticated(sessionQueryParam);

        if (authenticationInfo != null && authenticationInfo.isAuthenticated()) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(authenticationInfo
                                .getTenantDomain()
                        , true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(authenticationInfo.getUsername());
                if (deviceId != null && !deviceId.isEmpty() && deviceType != null && !deviceType.isEmpty()) {
                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(deviceId);
                    deviceIdentifier.setType(deviceType);

                    // Check authorization of user for given device
                    boolean userAuthorized = RemoteSessionManagementDataHolder.getInstance()
                            .getDeviceAccessAuthorizationService()
                            .isUserAuthorized(deviceIdentifier, authenticationInfo.getUsername());
                    if (userAuthorized) {
                        // set common settings for session
                        session.setMaxBinaryMessageBufferSize(RemoteSessionConstants.MAX_BUFFER_SIZE);
                        session.setMaxTextMessageBufferSize(RemoteSessionConstants.MAX_BUFFER_SIZE);
                        session.setMaxIdleTimeout(RemoteSessionManagementDataHolder.getInstance().getMaxIdleTimeout());

                        // if session initiated using operation id means request came from device
                        if (operationId != null) {
                            RemoteSession activeSession = RemoteSessionManagementDataHolder.getInstance()
                                    .getActiveDeviceClientSessionMap().get((authenticationInfo.getTenantDomain() + "/" +
                                            deviceType + "/" + deviceId));
                            if (activeSession != null) {
                                RemoteSession clientRemote = RemoteSessionManagementDataHolder.getInstance()
                                        .getSessionMap().get(activeSession.getMySession().getId());
                                if (clientRemote != null) {
                                    if (clientRemote.getOperationId().equals(operationId)) {
                                        RemoteSession deviceRemote = new RemoteSession(session, authenticationInfo
                                                .getTenantDomain(), deviceType, deviceId);
                                        deviceRemote.setOperationId(operationId);
                                        deviceRemote.setPeerSession(clientRemote);
                                        clientRemote.setPeerSession(deviceRemote);
                                        RemoteSessionManagementDataHolder.getInstance().getSessionMap().put(session
                                                .getId(), deviceRemote);
                                        // Send Remote connect response
                                        JSONObject message = new JSONObject();
                                        message.put(RemoteSessionConstants.REMOTE_CONNECT_CODE, RemoteSessionConstants
                                                .REMOTE_CONNECT);
                                        deviceRemote.sendMessageToPeer(message.toString());
                                        log.info("Device session opened for session id: " + session.getId() +
                                                " device Type : " + deviceType + " , " + "deviceId : " + deviceId);
                                    } else {
                                        throw new RemoteSessionManagementException("Device and Operation information " +
                                                "does not matched with client information for operation id: " +
                                                operationId + " device Type : " + deviceType + " , " + "deviceId : " +
                                                deviceId);
                                    }
                                } else {
                                    throw new RemoteSessionManagementException("Device session is inactive for " +
                                            "operation id: " + operationId + " device Type : " + deviceType + " , " +
                                            "deviceId : " + deviceId);
                                }


                            } else {
                                throw new RemoteSessionManagementException("Device session is inactive for operation " +
                                        "id: " + operationId + " device Type : " + deviceType + " , " + "deviceId : " +
                                        deviceId);
                            }
                        } else {
                            RemoteSession clientRemote = new RemoteSession(session, authenticationInfo
                                    .getTenantDomain(), deviceType, deviceId);
                            // Create new remote control operation to start the session
                            RemoteSession activeSession = RemoteSessionManagementDataHolder.getInstance()
                                    .getActiveDeviceClientSessionMap().putIfAbsent((authenticationInfo
                                                    .getTenantDomain() + "/" + deviceType + "/" + deviceId),
                                            clientRemote);
                            if (activeSession != null && activeSession.getMySession().isOpen() && activeSession
                                    .getPeerSession() == null) {
                                throw new RemoteSessionManagementException("Another client session waiting on device " +
                                        "to connect.");
                            } else {
                                // if there is pending session exists but already closed, then we need to remove it.
                                if (activeSession != null) {
                                    endSession(activeSession.getMySession(), "Remote session closed due to new session" +
                                            " request");
                                    // Use put if absent for adding session to waiting list since we need to overcome
                                    // multithreaded session requests.
                                    activeSession = RemoteSessionManagementDataHolder.getInstance()
                                            .getActiveDeviceClientSessionMap().putIfAbsent((authenticationInfo
                                                    .getTenantDomain() + "/" + deviceType + "/" +
                                                    deviceId), clientRemote);
                                }

                                // If another client tried to start session same time then active session will be
                                // exist. So we are adding session request only no parallel sessions added to map
                                if (activeSession == null) {

                                    // Create operation if session initiated by client
                                    Operation operation = new ConfigOperation();
                                    operation.setCode(RemoteSessionConstants.REMOTE_CONNECT);
                                    operation.setEnabled(true);
                                    operation.setControl(Operation.Control.NO_REPEAT);
                                    JSONObject payload = new JSONObject();
                                    payload.put("serverUrl", RemoteSessionManagementDataHolder.getInstance()
                                            .getServerUrl());
                                    operation.setPayLoad(payload.toString());
                                    String date = new SimpleDateFormat(RemoteSessionConstants.DATE_FORMAT_NOW).format
                                            (new Date());
                                    operation.setCreatedTimeStamp(date);
                                    List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
                                    deviceIdentifiers.add(new DeviceIdentifier(deviceId, deviceType));
                                    Activity activity = RemoteSessionManagementDataHolder.getInstance()
                                            .getDeviceManagementProviderService().addOperation(deviceType, operation,
                                                    deviceIdentifiers);
                                    clientRemote.setOperationId(activity.getActivityId()
                                            .replace(DeviceManagementConstants.OperationAttributes.ACTIVITY, ""));
                                    RemoteSessionManagementDataHolder.getInstance().getSessionMap().put(session.getId
                                            (), clientRemote);
                                    log.info("Client remote session opened for session id: " + session.getId() +
                                            " device Type : " + deviceType + " , " + "deviceId : " + deviceId);

                                } else {
                                    throw new RemoteSessionManagementException("Another client session waiting on " +
                                            "device to connect.");
                                }
                            }
                        }
                        log.info("Current remote sessions count: " + RemoteSessionManagementDataHolder.getInstance()
                                .getSessionMap().size());

                    } else {
                        throw new RemoteSessionManagementException("Missing device Id or type ");
                    }
                } else {
                    throw new RemoteSessionManagementException("Unauthorized Access for the device Type : " + deviceType
                            + " , deviceId : " + deviceId);
                }
            } catch (OperationManagementException | InvalidDeviceException e) {
                throw new RemoteSessionManagementException("Error occurred while adding initial operation for the " +
                        "device Type : " + deviceType + " , deviceId : " + deviceId);
            } catch (DeviceAccessAuthorizationException e) {
                throw new RemoteSessionManagementException("Error occurred while device access authorization for the " +
                        "device Type : " + deviceType + " , " + "deviceId : " + deviceId);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

        } else {
            throw new RemoteSessionManagementException("Invalid token");
        }
    }

    @Override
    public void initializeSession(Session session, String deviceType, String deviceId) throws
            RemoteSessionManagementException {
        initializeSession(session, deviceType, deviceId, null);
    }

    /**
     * Implements the behaviour of sending message to peer connection
     *
     * @param session Web socket RemoteSession
     * @param message String message needs to send to peer connection
     * @throws RemoteSessionManagementException throws when session cannot be made due to invalid data
     * @throws RemoteSessionManagementException throws when session has error with accessing device resources
     */
    @Override
    public void sendMessageToPeer(Session session, String message) throws RemoteSessionManagementException {
        JSONObject jsonObject = new JSONObject(message);
        RemoteSession remoteSession = RemoteSessionManagementDataHolder.getInstance().getSessionMap().get(session
                .getId());
        if (remoteSession != null) {
            remoteSession.sendMessageToPeer(jsonObject.toString());
        } else {
            throw new RemoteSessionManagementException("Remote Session cannot be found ");
        }
    }


    /**
     * Implements the behaviour of sending message to peer connection
     *
     * @param session Web socket RemoteSession
     * @param message Byte message needs to send to peer connection
     * @throws RemoteSessionManagementException throws when session cannot be made due to invalid data
     * @throws RemoteSessionManagementException throws when session has error with accessing device resources
     */
    @Override
    public void sendMessageToPeer(Session session, byte[] message) throws RemoteSessionManagementException {

        RemoteSession remoteSession = RemoteSessionManagementDataHolder.getInstance().getSessionMap().get(session
                .getId());
        if (remoteSession != null) {
            remoteSession.sendMessageToPeer(message);
        } else {
            throw new RemoteSessionManagementException("Remote Session cannot be found ");
        }
    }

    /**
     * Closing the session and cleanup the resources
     *
     * @param session Web socket Remote Session
     */
    @Override
    public void endSession(Session session, String closeReason) {
        log.info("Closing session: "+session.getId()+" due to:"+ closeReason);
        RemoteSession remoteSession = RemoteSessionManagementDataHolder.getInstance().getSessionMap().remove(session
                .getId());
        if (remoteSession != null) {
            String operationId = remoteSession.getOperationId();
            if (remoteSession.getPeerSession() != null) {
                Session peerSession = remoteSession.getPeerSession().getMySession();
                if (peerSession != null) {
                    RemoteSessionManagementDataHolder.getInstance().getSessionMap().remove(peerSession.getId());
                    if (peerSession.isOpen()) {
                        try {
                            peerSession.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, closeReason));
                        } catch (IOException ex) {
                            if (log.isDebugEnabled()) {
                                log.error("Failed to disconnect the client.", ex);
                            }
                        }
                    }
                }
            }
            if (remoteSession.getMySession() != null) {
                Session mySession = remoteSession.getMySession();
                if (mySession.isOpen()) {
                    try {
                        mySession.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, closeReason));
                    } catch (IOException ex) {
                        if (log.isDebugEnabled()) {
                            log.error("Failed to disconnect the client.", ex);
                        }
                    }
                }
            }
            if (operationId != null) {
                String deviceIdentifier = remoteSession.getTenantDomain() + "/" + remoteSession
                        .getDeviceType() + "/" + remoteSession.getDeviceId();
                RemoteSession lastSession = RemoteSessionManagementDataHolder.getInstance()
                        .getActiveDeviceClientSessionMap().get(deviceIdentifier);
                if (lastSession != null && lastSession.getMySession().getId().equals(session.getId())) {
                    RemoteSessionManagementDataHolder.getInstance().getActiveDeviceClientSessionMap().remove
                            (deviceIdentifier);
                }
            }
        }

    }
}
