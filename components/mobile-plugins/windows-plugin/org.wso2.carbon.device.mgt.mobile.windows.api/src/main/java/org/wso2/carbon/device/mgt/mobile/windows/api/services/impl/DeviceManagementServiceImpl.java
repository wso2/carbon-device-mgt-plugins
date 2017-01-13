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

package org.wso2.carbon.device.mgt.mobile.windows.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.device.details.*;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.PluginConstants;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.beans.CacheEntry;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.SyncmlMessageFormatException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.SyncmlOperationException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsDeviceEnrolmentException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.AuthenticationInfo;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.DeviceUtil;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.WindowsAPIUtils;
import org.wso2.carbon.device.mgt.mobile.windows.api.operations.*;
import org.wso2.carbon.device.mgt.mobile.windows.api.operations.util.*;
import org.wso2.carbon.device.mgt.mobile.windows.api.operations.util.DeviceInfo;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.DeviceManagementService;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.device.mgt.mobile.windows.api.common.util.WindowsAPIUtils.convertToDeviceIdentifierObject;


public class DeviceManagementServiceImpl implements DeviceManagementService {
    private static Log log = LogFactory.getLog(
            org.wso2.carbon.device.mgt.mobile.windows.api.services.syncml.impl.SyncmlServiceImpl.class);

    @Override
    public Response getResponse(Document request) throws WindowsDeviceEnrolmentException, WindowsOperationException,
            NotificationManagementException, WindowsConfigurationException {

        int msgId;
        int sessionId;
        String user;
        String token;
        String response;
        SyncmlDocument syncmlDocument;
        List<? extends Operation> pendingOperations;
        OperationHandler operationHandler = new OperationHandler();
        OperationReply operationReply = new OperationReply();

        try {
            if (SyncmlParser.parseSyncmlPayload(request) != null) {
                syncmlDocument = SyncmlParser.parseSyncmlPayload(request);
                SyncmlHeader syncmlHeader = syncmlDocument.getHeader();
                sessionId = syncmlHeader.getSessionId();
                user = syncmlHeader.getSource().getLocName();
                DeviceIdentifier deviceIdentifier = convertToDeviceIdentifierObject(syncmlHeader.getSource().
                        getLocURI());
                msgId = syncmlHeader.getMsgID();
                if ((PluginConstants.SyncML.SYNCML_FIRST_MESSAGE_ID == msgId) &&
                        (PluginConstants.SyncML.SYNCML_FIRST_SESSION_ID == sessionId)) {
                    token = syncmlHeader.getCredential().getData();
                    CacheEntry cacheToken = (CacheEntry) DeviceUtil.getCacheEntry(token);

                    if ((cacheToken.getUsername() != null) && (cacheToken.getUsername().equals(user))) {

                        if (modifyEnrollWithMoreDetail(request)) {
                            pendingOperations = operationHandler.getPendingOperations(syncmlDocument);
                            response = operationReply.generateReply(syncmlDocument, pendingOperations);
                            return Response.status(Response.Status.OK).entity(response).build();
                        } else {
                            String msg = "Error occurred in while modify the enrollment.";
                            log.error(msg);
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
                        }
                    } else {
                        String msg = "Authentication failure due to incorrect credentials.";
                        log.error(msg);
                        return Response.status(Response.Status.UNAUTHORIZED).entity(msg).build();
                    }
                } else {
                    if ((syncmlDocument.getBody().getAlert() != null)) {
                        if (!syncmlDocument.getBody().getAlert().getData().equals(Constants.DISENROLL_ALERT_DATA)) {
                            pendingOperations = operationHandler.getPendingOperations(syncmlDocument);
                            return Response.ok().entity(operationReply.generateReply(
                                    syncmlDocument, pendingOperations)).build();
                        } else {
                            if (WindowsAPIUtils.getDeviceManagementService().getDevice(deviceIdentifier) != null) {
                                WindowsAPIUtils.getDeviceManagementService().disenrollDevice(deviceIdentifier);
                                return Response.ok().entity(operationReply.generateReply(syncmlDocument, null)).build();
                            } else {
                                String msg = "Enrolled device can not be found in the server.";
                                log.error(msg);
                                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
                            }
                        }
                    } else {
                        pendingOperations = operationHandler.getPendingOperations(syncmlDocument);
                        return Response.ok().entity(operationReply.generateReply(
                                syncmlDocument, pendingOperations)).build();
                    }
                }
            }
        } catch (SyncmlMessageFormatException e) {
            String msg = "Error occurred while parsing syncml request.";
            log.error(msg, e);
            throw new WindowsOperationException(msg, e);
        } catch (OperationManagementException e) {
            String msg = "Cannot access operation management service.";
            log.error(msg, e);
            throw new WindowsOperationException(msg, e);
        } catch (SyncmlOperationException e) {
            String msg = "Error occurred while getting effective feature.";
            log.error(msg, e);
            throw new WindowsConfigurationException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "Failure occurred in dis-enrollment flow.";
            log.error(msg, e);
            throw new WindowsOperationException(msg, e);
        }
        return null;
    }

    /**
     * Enroll phone device
     *
     * @param request Device syncml request for the server side.
     * @return enroll state
     * @throws WindowsDeviceEnrolmentException
     * @throws WindowsOperationException
     */
    private boolean modifyEnrollWithMoreDetail(Document request) throws WindowsDeviceEnrolmentException,
            WindowsOperationException {

        String devMan = null;
        String devMod = null;
        boolean status = false;
        String user;
        SyncmlDocument syncmlDocument;

        try {
            syncmlDocument = SyncmlParser.parseSyncmlPayload(request);
            ReplaceTag replace = syncmlDocument.getBody().getReplace();
            List<ItemTag> itemList = replace.getItems();
            for (ItemTag itemTag : itemList) {
                String locURI = itemTag.getSource().getLocURI();
                if (OperationCode.Info.MANUFACTURER.getCode().equals(locURI)) {
                    devMan = itemTag.getData();
                }
                if (OperationCode.Info.DEVICE_MODEL.getCode().equals(locURI)) {
                    devMod = itemTag.getData();
                }
            }
            user = syncmlDocument.getHeader().getSource().getLocName();
            AuthenticationInfo authenticationInfo = new AuthenticationInfo();
            authenticationInfo.setUsername(user);
            WindowsAPIUtils.startTenantFlow(authenticationInfo);
            DeviceIdentifier deviceIdentifier = convertToDeviceIdentifierObject(syncmlDocument.
                    getHeader().getSource().getLocURI());
            Device existingDevice = WindowsAPIUtils.getDeviceManagementService().getDevice(deviceIdentifier);
            if (!existingDevice.getProperties().isEmpty()) {
                List<Device.Property> existingProperties = new ArrayList<>();

                Device.Property vendorProperty = new Device.Property();
                vendorProperty.setName(PluginConstants.SyncML.VENDOR);
                vendorProperty.setValue(devMan);
                existingProperties.add(vendorProperty);

                Device.Property deviceModelProperty = new Device.Property();
                deviceModelProperty.setName(PluginConstants.SyncML.MODEL);
                deviceModelProperty.setValue(devMod);
                existingProperties.add(deviceModelProperty);

                existingDevice.setProperties(existingProperties);
                existingDevice.setDeviceIdentifier(syncmlDocument.getHeader().getSource().getLocURI());
                existingDevice.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
                status = WindowsAPIUtils.getDeviceManagementService().modifyEnrollment(existingDevice);
                return status;
            }
        } catch (DeviceManagementException e) {
            throw new WindowsDeviceEnrolmentException("Failure occurred while enrolling device.", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return status;
    }
}
