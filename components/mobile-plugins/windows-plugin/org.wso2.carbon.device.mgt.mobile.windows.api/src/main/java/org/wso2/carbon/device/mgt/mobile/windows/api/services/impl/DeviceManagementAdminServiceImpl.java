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

import com.ibm.wsdl.OperationImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.PluginConstants;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.BadRequestException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsDeviceEnrolmentException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsOperationsException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.Message;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.WindowsAPIUtils;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.DeviceManagementAdminService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Implementation class of operations interface. Each method in this class receives the operations comes via UI
 * and persists those in the correct format.
 */
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Path("/admin/devices")
public class DeviceManagementAdminServiceImpl implements DeviceManagementAdminService {

    private static Log log = LogFactory.getLog(OperationImpl.class);

    /**
     * REST endpoint for the Device Lock operation
     *
     * @param acceptHeader header value of the request POST message.
     * @param deviceIDs    list of device ids to be add device updateLockOperation operation.
     * @return Response object for client.
     * @throws WindowsDeviceEnrolmentException
     */
    @POST
    @Path("/lock-devices")
    public Response lock(@HeaderParam("Accept") String acceptHeader, List<String> deviceIDs)
            throws WindowsDeviceEnrolmentException {
        if (log.isDebugEnabled()) {
            log.debug("Invoking windows device updateLockOperation operation");
        }
        MediaType responseMediaType = WindowsAPIUtils.getResponseMediaType(acceptHeader);
        Message message = new Message();
        try {
            CommandOperation operation = new CommandOperation();
            operation.setCode(PluginConstants.OperationCodes.DEVICE_LOCK);
            operation.setType(Operation.Type.COMMAND);
            operation.setEnabled(true);
            return WindowsAPIUtils.getOperationResponse(deviceIDs, operation);
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            message.setResponseMessage(errorMessage);
            message.setResponseCode(Response.Status.INTERNAL_SERVER_ERROR.toString());
            log.error(errorMessage, e);
            throw new WindowsOperationsException(message, responseMediaType);
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving device management service instance";
            message.setResponseMessage(errorMessage);
            message.setResponseCode(Response.Status.INTERNAL_SERVER_ERROR.toString());
            log.error(errorMessage, e);
            throw new WindowsOperationsException(message, responseMediaType);
        } catch (InvalidDeviceException e) {
            String errorMessage = "Invalid Device Identifiers found.";
            log.error(errorMessage, e);
            throw new BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(errorMessage).build());
        }
    }

    /**
     * REST end point for device dis-enrollment.
     *
     * @param acceptHeader POST message header value.
     * @param deviceIDs    device ids to be dis-enrolled.
     * @return Response object to the client.
     * @throws WindowsDeviceEnrolmentException
     */
    @POST
    @Path("/disenroll-devices")
    public Response disenroll(@HeaderParam("Accept") String acceptHeader, List<String> deviceIDs)
            throws WindowsDeviceEnrolmentException {

        MediaType responseMediaType = WindowsAPIUtils.getResponseMediaType(acceptHeader);
        Message message = new Message();
        CommandOperation operation = new CommandOperation();
        operation.setCode(PluginConstants.OperationCodes.DISENROLL);
        operation.setType(Operation.Type.COMMAND);
        operation.setEnabled(true);
        try {
            return WindowsAPIUtils.getOperationResponse(deviceIDs, operation);
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            message.setResponseMessage(errorMessage);
            message.setResponseCode(Response.Status.INTERNAL_SERVER_ERROR.toString());
            log.error(errorMessage, e);
            throw new WindowsOperationsException(message, responseMediaType);
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving device management service instance";
            message.setResponseMessage(errorMessage);
            message.setResponseCode(Response.Status.INTERNAL_SERVER_ERROR.toString());
            log.error(errorMessage, e);
            throw new WindowsOperationsException(message, responseMediaType);
        } catch (InvalidDeviceException e) {
            String errorMessage = "Invalid Device Identifiers found.";
            log.error(errorMessage, e);
            throw new BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(errorMessage).build());
        }
    }

    /**
     * REST Endpoint for the Device wipe.
     *
     * @param acceptHeader POST message header value.
     * @param deviceids    device ids to be wiped.
     * @return Response object for the client.
     * @throws WindowsDeviceEnrolmentException
     */
    @POST
    @Path("/wipe-devices")
    public Response wipe(@HeaderParam("Accept") String acceptHeader, List<String> deviceids)
            throws WindowsDeviceEnrolmentException {

        if (log.isDebugEnabled()) {
            log.debug("Invoking windows wipe-data device operation");
        }
        MediaType responseMediaType = WindowsAPIUtils.getResponseMediaType(acceptHeader);
        Message message = new Message();
        CommandOperation operation = new CommandOperation();
        operation.setCode(PluginConstants.OperationCodes.WIPE_DATA);
        operation.setType(Operation.Type.COMMAND);
        try {
            return WindowsAPIUtils.getOperationResponse(deviceids, operation);
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            message.setResponseMessage(errorMessage);
            message.setResponseCode(Response.Status.INTERNAL_SERVER_ERROR.toString());
            log.error(errorMessage, e);
            throw new WindowsOperationsException(message, responseMediaType);
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving device management service instance";
            message.setResponseMessage(errorMessage);
            message.setResponseCode(Response.Status.INTERNAL_SERVER_ERROR.toString());
            log.error(errorMessage, e);
            throw new WindowsOperationsException(message, responseMediaType);
        } catch (InvalidDeviceException e) {
            String errorMessage = "Invalid Device Identifiers found.";
            log.error(errorMessage, e);
            throw new BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(errorMessage).build());
        }
    }

    /**
     * REST end point for the device ring.
     *
     * @param acceptHeader post message header value.
     * @param deviceIDs    device ids to be ring.
     * @return Response object for the client.
     * @throws WindowsDeviceEnrolmentException
     */
    @POST
    @Path("/ring-devices")
    public Response ring(@HeaderParam("Accept") String acceptHeader, List<String> deviceIDs)
            throws WindowsDeviceEnrolmentException {

        if (log.isDebugEnabled()) {
            log.debug("Invoking Windows ring-device device operation");
        }
        MediaType responseMediaType = WindowsAPIUtils.getResponseMediaType(acceptHeader);
        Message message = new Message();
        try {
            CommandOperation operation = new CommandOperation();
            operation.setCode(PluginConstants.OperationCodes.DEVICE_RING);
            operation.setType(Operation.Type.COMMAND);
            return WindowsAPIUtils.getOperationResponse(deviceIDs, operation);
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            message.setResponseMessage(errorMessage);
            message.setResponseCode(Response.Status.INTERNAL_SERVER_ERROR.toString());
            log.error(errorMessage, e);
            throw new WindowsOperationsException(message, responseMediaType);
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving device management service instance";
            message.setResponseMessage(errorMessage);
            message.setResponseCode(Response.Status.INTERNAL_SERVER_ERROR.toString());
            log.error(errorMessage, e);
            throw new WindowsOperationsException(message, responseMediaType);
        } catch (InvalidDeviceException e) {
            String errorMessage = "Invalid Device Identifiers found.";
            log.error(errorMessage, e);
            throw new BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(errorMessage).build());
        }
    }

    /**
     * REST endpoint for the device updateLockOperation reset.
     * Lock reset have to be done, when device user does not set PIN for the updateLockOperation screen.
     * Admin set updateLockOperation operation for the specific device,If the device is in above scenario,
     * admin will be notified.since
     * admin have to set updateLockOperation reset operation to the device so that automatically generate PIN value for the
     * updateLockOperation screen.
     *
     * @param acceptHeader POST message header value.
     * @param deviceIDs    Device ids to be updateLockOperation reset.
     * @return Response object for the client.
     * @throws WindowsDeviceEnrolmentException
     */
    @POST
    @Path("/lock-reset-devices")
    public Response lockReset(@HeaderParam("Accept") String acceptHeader, List<String> deviceIDs)
            throws WindowsDeviceEnrolmentException {

        if (log.isDebugEnabled()) {
            log.debug("Invoking windows device lockReset storage operation");
        }
        MediaType responseMediaType = WindowsAPIUtils.getResponseMediaType(acceptHeader);
        Message message = new Message();
        try {
            CommandOperation operation = new CommandOperation();
            operation.setCode(PluginConstants.OperationCodes.LOCK_RESET);
            operation.setType(Operation.Type.COMMAND);
            return WindowsAPIUtils.getOperationResponse(deviceIDs, operation);
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            message.setResponseMessage(errorMessage);
            message.setResponseCode(Response.Status.INTERNAL_SERVER_ERROR.toString());
            log.error(errorMessage, e);
            throw new WindowsOperationsException(message, responseMediaType);
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving device management service instance";
            message.setResponseMessage(errorMessage);
            message.setResponseCode(Response.Status.INTERNAL_SERVER_ERROR.toString());
            log.error(errorMessage, e);
            throw new WindowsOperationsException(message, responseMediaType);
        } catch (InvalidDeviceException e) {
            String errorMessage = "Invalid Device Identifiers found.";
            log.error(errorMessage, e);
            throw new BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(errorMessage).build());
        }
    }
}
