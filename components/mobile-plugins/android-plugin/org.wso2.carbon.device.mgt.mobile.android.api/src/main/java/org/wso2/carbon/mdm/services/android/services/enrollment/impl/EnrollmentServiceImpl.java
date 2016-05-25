/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.services.android.services.enrollment.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.mdm.services.android.exception.AndroidAgentException;
import org.wso2.carbon.mdm.services.android.services.enrollment.EnrollmentService;
import org.wso2.carbon.mdm.services.android.util.AndroidAPIUtils;
import org.wso2.carbon.mdm.services.android.util.Message;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Android Device Enrollment REST-API implementation.
 * All end points supports JSON, XMl with content negotiation.
 */
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public class EnrollmentServiceImpl implements EnrollmentService {

    private static Log log = LogFactory.getLog(EnrollmentService.class);

    @POST
    public Response enrollDevice(org.wso2.carbon.device.mgt.common.Device device)
            throws AndroidAgentException {

        Message responseMsg = new Message();
        String msg;
        try {
            device.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
            device.getEnrolmentInfo().setOwner(AndroidAPIUtils.getAuthenticatedUser());
            boolean status = AndroidAPIUtils.getDeviceManagementService().enrollDevice(device);
            if (status) {
                Response.status(Response.Status.CREATED);
                responseMsg.setResponseMessage("Device enrollment succeeded.");
                return Response.status(Response.Status.CREATED).entity(responseMsg).build();
            } else {
                Response.status(Response.Status.INTERNAL_SERVER_ERROR);
                responseMsg.setResponseMessage("Device enrollment failed.");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseMsg).build();
            }
        } catch (DeviceManagementException e) {
            msg = "Error occurred while enrolling the device";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("{deviceId}")
    public Response isEnrolled(@PathParam("deviceId") String id) throws AndroidAgentException {
        String msg;
        boolean result;
        Message responseMsg = new Message();
        DeviceIdentifier deviceIdentifier = AndroidAPIUtils.convertToDeviceIdentifierObject(id);

        try {
            result = AndroidAPIUtils.getDeviceManagementService().isEnrolled(deviceIdentifier);
            if (result) {
                responseMsg.setResponseMessage("Device has already enrolled");
                responseMsg.setResponseCode(Response.Status.ACCEPTED.toString());
                return Response.status(Response.Status.ACCEPTED).entity(responseMsg).build();
            } else {
                responseMsg.setResponseMessage("Device not found");
                responseMsg.setResponseCode(Response.Status.NOT_FOUND.toString());
                return Response.status(Response.Status.NOT_FOUND).entity(responseMsg).build();
            }
        } catch (DeviceManagementException e) {
            msg = "Error occurred while checking enrollment status of the device.";
            responseMsg.setResponseMessage(msg);
            responseMsg.setResponseCode(Response.Status.INTERNAL_SERVER_ERROR.toString());
            Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("{deviceId}")
    public Response modifyEnrollment(@PathParam("deviceId") String id,
                                    org.wso2.carbon.device.mgt.common.Device device)
            throws AndroidAgentException {
        String msg;
        boolean result;
        Message responseMsg = new Message();
        try {
            device.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
            result = AndroidAPIUtils.getDeviceManagementService().modifyEnrollment(device);
            if (result) {
                responseMsg.setResponseMessage("Device enrollment has updated successfully");
                responseMsg.setResponseCode(Response.Status.ACCEPTED.toString());
                return Response.status(Response.Status.ACCEPTED).entity(responseMsg).build();
            } else {
                responseMsg.setResponseMessage("Device not found for enrollment");
                responseMsg.setResponseCode(Response.Status.NOT_MODIFIED.toString());
                return Response.status(Response.Status.NOT_MODIFIED).entity(responseMsg).build();
            }
        } catch (DeviceManagementException e) {
            msg = "Error occurred while modifying enrollment of the device";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Path("{deviceId}")
    public Response disEnrollDevice(@PathParam("deviceId") String id) throws AndroidAgentException {
        Message responseMsg = new Message();
        boolean result;
        String msg;
        DeviceIdentifier deviceIdentifier = AndroidAPIUtils.convertToDeviceIdentifierObject(id);

        try {
            result = AndroidAPIUtils.getDeviceManagementService().disenrollDevice(deviceIdentifier);
            if (result) {
                responseMsg.setResponseMessage("Device has removed successfully");
                responseMsg.setResponseCode(Response.Status.ACCEPTED.toString());
                return Response.status(Response.Status.ACCEPTED).entity(responseMsg).build();
            } else {
                responseMsg.setResponseMessage("Device not found");
                responseMsg.setResponseCode(Response.Status.NOT_FOUND.toString());
                return Response.status(Response.Status.NOT_FOUND).entity(responseMsg).build();
            }
        } catch (DeviceManagementException e) {
            msg = "Error occurred while dis enrolling the device";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
