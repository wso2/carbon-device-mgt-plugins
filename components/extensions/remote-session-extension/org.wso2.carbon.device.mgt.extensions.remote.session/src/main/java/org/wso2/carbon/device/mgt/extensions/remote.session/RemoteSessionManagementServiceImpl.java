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
import org.wso2.carbon.device.mgt.extensions.remote.session.dto.ClientSession;
import org.wso2.carbon.device.mgt.extensions.remote.session.dto.DeviceSession;
import org.wso2.carbon.device.mgt.extensions.remote.session.dto.common.RemoteSession;
import org.wso2.carbon.device.mgt.extensions.remote.session.exception.RemoteSessionInvalidException;
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
    private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    private static final int MAX_BUFFER_SIZE = 640 * 1024;


    @Override
    public void initializeSession(Session session, String deviceType, String deviceId, String operationId) throws
            RemoteSessionInvalidException, RemoteSessionManagementException {


        if (!RemoteSessionManagementDataHolder.getInstance().isEnabled()) {
            throw new RemoteSessionManagementException("Remote session feature is disabled.");
        } else if (RemoteSessionManagementDataHolder.getInstance().getServerUrl() == null) {
            throw new RemoteSessionManagementException("Server url haven't been configured.");
        }

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
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(authenticationInfo.getTenantDomain()
                        , true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(authenticationInfo.getUsername());
                if (deviceId != null && !deviceId.isEmpty() && deviceType != null && !deviceType.isEmpty()) {
                    DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(deviceId);
                    deviceIdentifier.setType(deviceType);
                    // Check authorization for user
                    boolean userAuthorized = RemoteSessionManagementDataHolder.getInstance()
                            .getDeviceAccessAuthorizationService()
                            .isUserAuthorized(deviceIdentifier, authenticationInfo.getUsername());
                    if (userAuthorized) {
                        log.info("Operation ID: " + operationId);

                        // set common settings for session
                        session.setMaxBinaryMessageBufferSize(MAX_BUFFER_SIZE);
                        session.setMaxTextMessageBufferSize(MAX_BUFFER_SIZE);
                        session.setMaxIdleTimeout(RemoteSessionManagementDataHolder.getInstance().getMaxIdleTimeout());

                        // if session initiated using operatiod id means request came from device
                        if (operationId != null) {
                            Session pendingSession = RemoteSessionManagementDataHolder.getInstance()
                                    .getDeviceRequestMap().get((authenticationInfo.getTenantDomain() + "/" + deviceType
                                            + "/" + deviceId));

                            if (pendingSession != null) {
                                RemoteSession clientRemote = RemoteSessionManagementDataHolder.getInstance()
                                        .getSessionMap().get(pendingSession.getId());
                                if (clientRemote != null) {

                                    if (clientRemote.getOperationId().equals(operationId)) {
                                        RemoteSession deviceRemote = new DeviceSession(session, authenticationInfo
                                                .getTenantDomain(), deviceType, deviceId, operationId);
                                        deviceRemote.setPeerSession(clientRemote);
                                        clientRemote.setPeerSession(deviceRemote);
                                        RemoteSessionManagementDataHolder.getInstance().getSessionMap().put(session
                                                .getId(), deviceRemote);
                                        RemoteSessionManagementDataHolder.getInstance().getDeviceRequestMap().remove(
                                                (authenticationInfo.getTenantDomain() + "/" + deviceType + "/" + deviceId));
                                        // Send Remote connect response
                                        JSONObject message = new JSONObject();
                                        message.put("code", RemoteSessionConstants.REMOTE_CONNECT);
                                        message.put("operation_response", "connected");
                                        deviceRemote.sendMessageToPeer(message.toString());

                                    } else {
                                        throw new RemoteSessionManagementException("Device and Operation information does" +
                                                " not matched with client information for operation id: " + operationId +
                                                " device Type : " + deviceType + " , " + "deviceId : " +
                                                deviceId);
                                    }
                                } else {
                                    throw new RemoteSessionManagementException("Device session is inactive for operation " +
                                            "id: " + operationId + " device Type : " + deviceType + " , " + "deviceId : " +
                                            deviceId);
                                }


                            } else {
                                throw new RemoteSessionManagementException("Device session is inactive for operation " +
                                        "id: " + operationId + " device Type : " + deviceType + " , " + "deviceId : " +
                                        deviceId);
                            }

                        } else {
                            // Create new remote control operation to start the session
                            Session pendingSession = RemoteSessionManagementDataHolder.getInstance().getDeviceRequestMap().get(
                                    (authenticationInfo.getTenantDomain() + "/" + deviceType + "/" + deviceId));
                            if (pendingSession != null && pendingSession.isOpen()) {
                                throw new RemoteSessionManagementException("Another client session waiting on device to connect.");
                            } else {
                                Session lastSession = RemoteSessionManagementDataHolder.getInstance().getDeviceRequestMap().putIfAbsent(
                                        (authenticationInfo.getTenantDomain() + "/" + deviceType + "/" + deviceId),
                                        session);

                                if (lastSession == null) {

                                    // Create operation if session initiated by client
                                    Operation operation = new ConfigOperation();
                                    operation.setCode(RemoteSessionConstants.REMOTE_CONNECT);
                                    operation.setEnabled(true);
                                    operation.setControl(Operation.Control.NO_REPEAT);
                                    JSONObject payload = new JSONObject();
                                    payload.put("serverUrl", RemoteSessionManagementDataHolder.getInstance().getServerUrl());
                                    operation.setPayLoad(payload.toString());
                                    String date = new SimpleDateFormat(DATE_FORMAT_NOW).format(new Date());
                                    operation.setCreatedTimeStamp(date);

                                    List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
                                    deviceIdentifiers.add(new DeviceIdentifier(deviceId, deviceType));
                                    Activity activity = RemoteSessionManagementDataHolder.getInstance()
                                            .getDeviceManagementProviderService().addOperation(deviceType, operation,
                                                    deviceIdentifiers);
                                    log.info("Activity id: " + activity.getActivityId());

                                    RemoteSession clientRemote = new ClientSession(session, authenticationInfo
                                            .getTenantDomain(), deviceType, deviceId, activity.getActivityId().replace(DeviceManagementConstants
                                            .OperationAttributes.ACTIVITY, ""));
                                    RemoteSessionManagementDataHolder.getInstance().getSessionMap().put(session.getId(), clientRemote);
                                }
                            }
                        }
                        log.info("Current session count: " + RemoteSessionManagementDataHolder
                                .getInstance().getSessionMap().size());

                    } else {
                        throw new RemoteSessionInvalidException("Missing device Id or type ", new CloseReason
                                (CloseReason.CloseCodes.CANNOT_ACCEPT, "Missing device Id or device type "));
                    }
                } else {
                    throw new RemoteSessionInvalidException("Unauthorized Access for the device Type : " + deviceType
                            + " , deviceId : " + deviceId, new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
                            "Unauthorized Access"));
                }
            } catch (OperationManagementException | InvalidDeviceException e) {
                throw new RemoteSessionManagementException("Error occurred while adding initial operation for the device Type : " +
                        deviceType + " , deviceId : " + deviceId, e);
            } catch (DeviceAccessAuthorizationException e) {
                throw new RemoteSessionManagementException("Error occurred while device access authorization for the device Type : " +
                        deviceType + " , " + "deviceId : " + deviceId, e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

        } else {
            throw new RemoteSessionInvalidException("Invalid token", new CloseReason(CloseReason.CloseCodes
                    .CANNOT_ACCEPT, "Invalid token"));
        }
    }

    @Override
    public void initializeSession(Session session, String deviceType, String deviceId) throws RemoteSessionInvalidException, RemoteSessionManagementException {
        initializeSession(session, deviceType, deviceId, null);
    }

    /**
     * Implements the behaviour of sending message to peer connection
     *
     * @param session Web socket RemoteSession
     * @param message String message needs to send to peer connection
     * @throws RemoteSessionInvalidException    throws when session cannot be made due to invalid data
     * @throws RemoteSessionManagementException throws when session has error with accessing device resources
     */
    @Override
    public void sendMessageToPeer(Session session, String message) throws RemoteSessionManagementException,
            RemoteSessionInvalidException {
        JSONObject jsonObject = new JSONObject(message);
        RemoteSession remoteSession = RemoteSessionManagementDataHolder.getInstance().getSessionMap().get(session.getId());
        if (remoteSession != null) {
            if (remoteSession instanceof ClientSession) {
                jsonObject.put("id", remoteSession.getOperationId());
            }
            remoteSession.sendMessageToPeer(jsonObject.toString());
        } else {
            throw new RemoteSessionInvalidException("Remote Session cannot be found ", new CloseReason(CloseReason
                    .CloseCodes.CANNOT_ACCEPT, "Invalid RemoteSession"));
        }
    }


    /**
     * Implements the behaviour of sending message to peer connection
     *
     * @param session Web socket RemoteSession
     * @param message Byte message needs to send to peer connection
     * @throws RemoteSessionInvalidException    throws when session cannot be made due to invalid data
     * @throws RemoteSessionManagementException throws when session has error with accessing device resources
     */
    @Override
    public void sendMessageToPeer(Session session, byte[] message) throws RemoteSessionInvalidException,
            RemoteSessionManagementException {

        RemoteSession remoteSession = RemoteSessionManagementDataHolder.getInstance().getSessionMap().get(session.getId());
        if (remoteSession != null) {
            remoteSession.sendMessageToPeer(message);
        } else {
            throw new RemoteSessionInvalidException("Remote Session cannot be found ", new CloseReason(CloseReason
                    .CloseCodes.CANNOT_ACCEPT, "Invalid RemoteSession"));
        }
    }

    /**
     * Closing the session and cleanup the resources
     *
     * @param session Web socket RemoteSession
     */
    @Override
    public void endSession(Session session) throws IOException {

        RemoteSession remoteSession = RemoteSessionManagementDataHolder.getInstance().getSessionMap().remove(session.getId());
        if (remoteSession != null) {
            String operationId = remoteSession.getOperationId();
            Session peerSession = remoteSession.getPeerSession().getMySession();
            if (peerSession != null) {
                RemoteSessionManagementDataHolder.getInstance().getSessionMap().remove(peerSession.getId());
                if (peerSession.isOpen()) {
                    peerSession.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "Remote session closed"));
                }
            }
            if (operationId != null) {
                Session lastSession = RemoteSessionManagementDataHolder.getInstance().getDeviceRequestMap().get(
                        (remoteSession.getTenantDomain() + "/" + remoteSession.getDeviceType() + "/" + remoteSession
                                .getDeviceId()));
                if (lastSession != null && lastSession.getId().equals(session.getId())) {
                    RemoteSessionManagementDataHolder.getInstance().getDeviceRequestMap().remove(
                            (remoteSession.getTenantDomain() + "/" + remoteSession.getDeviceType() + "/" + remoteSession
                                    .getDeviceId()));
                }
            }
        }

    }
}
