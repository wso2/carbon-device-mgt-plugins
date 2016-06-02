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

package org.wso2.carbon.mdm.services.android.services.enrollment;

import io.swagger.annotations.*;
import org.wso2.carbon.mdm.services.android.exception.AndroidAgentException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Android Device Enrollment REST-API implementation.
 * All end points supports JSON, XMl with content negotiation.
 */

@Api(value = "EnrollmentService", description = "Android Device Enrollment REST-API implementation.")
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface EnrollmentService {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Registering an Android Device",
            notes = "When carrying out device registration via an Android device, you need to initially install" +
                    " an Android Agent on the device, before being able to register the device with WSO2 EMM. Instead," +
                    " you can use this REST API to register an Android device with WSO2 EMM, without having to install" +
                    " an Android Agent on the respective device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Device enrollment succeeded"),
            @ApiResponse(code = 500, message = "Device enrollment failed"),
    })
    Response enrollDevice(@ApiParam(name = "device", value = "Device Information to be enroll")
                                 org.wso2.carbon.device.mgt.common.Device device) throws AndroidAgentException;

    @GET
    @Path("{deviceId}")
    @ApiOperation(
            httpMethod = "GET",
            value = "Getting the Registration Status of an Android Device",
            notes = "Use this REST API to retrieve the registration status of an Android device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Device has already enrolled"),
            @ApiResponse(code = 404, message = "Device not found")
    })
    Response isEnrolled(@ApiParam(name = "deviceId", value = "DeviceIdentifier") @PathParam("deviceId") String id)
            throws AndroidAgentException;

    @PUT
    @Path("{deviceId}")
    @ApiOperation(
            httpMethod = "PUT",
            value = "Updating the Registration Details of an Android Device",
            notes = "Use this REST API to update the registration details of an Android device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Device enrollment has updated successfully"),
            @ApiResponse(code = 404, message = "Device not found for enrollment")
    })
    Response modifyEnrollment(@ApiParam(name = "deviceId", value = "DeviceIdentifier") @PathParam("deviceId") String id,
                             @ApiParam(name = "device", value = "Device information to be modify")
                                     org.wso2.carbon.device.mgt.common.Device device)
            throws AndroidAgentException;

    @DELETE
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Un-registering an Android Device",
            notes = "Use this REST API to unregister a specific Android device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Device has removed successfully"),
            @ApiResponse(code = 404, message = "Device not found")
    })
    @Path("{deviceId}")
    Response disEnrollDevice(@ApiParam(name = "deviceId", value = "DeviceIdentifier") @PathParam("deviceId") String id)
            throws AndroidAgentException;
}
