/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.mdm.services.android.services;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceManagementService {

    @PUT
    @Path("/{id}/applications")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating an ApplicationList",
            notes = "Update application list in server side."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Device information has modified successfully"),
            @ApiResponse(code = 500, message = "Error occurred while modifying the application list")
    })
    Response updateApplicationList(@ApiParam(name = "id", value = "deviceIdentifier") @PathParam("id") String id,
                                   @ApiParam(name = "applications", value = "updatable applications")
                                   List<Application> applications);

    @GET
    @Path("/{id}/pending-operations")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Getting Pending Android Device Operations",
            responseContainer = "List",
            notes = "The Android agent communicates with the server to get the operations that are queued up " +
                    "at the server end for a given device using this REST API",
            response = Operation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of pending operations"),
            @ApiResponse(code = 500, message = "Issue in retrieving operation management service instance")
    })
    Response getPendingOperations(
            @ApiParam(name = "id", value = "DeviceIdentifier") @PathParam("id") String id,
            @ApiParam(name = "resultOperations", value = "Device Operation Status")
            List<? extends Operation> resultOperations);

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
            @ApiResponse(code = 200, message = "Device enrollment succeeded"),
            @ApiResponse(code = 500, message = "Device enrollment failed"),
    })
    Response enrollDevice(@ApiParam(name = "device", value = "Device Information to be enroll") Device device);

    @GET
    @Path("/{id}/status")
    @ApiOperation(
            httpMethod = "GET",
            value = "Getting the Registration Status of an Android Device",
            notes = "Use this REST API to retrieve the registration status of an Android device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Device has already enrolled"),
            @ApiResponse(code = 404, message = "Device not found")
    })
    Response isEnrolled(@ApiParam(name = "id", value = "DeviceIdentifier") @PathParam("id") String id);

    @PUT
    @Path("/{id}")
    @ApiOperation(
            httpMethod = "PUT",
            value = "Updating the Registration Details of an Android Device",
            notes = "Use this REST API to update the registration details of an Android device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Device enrollment has updated successfully"),
            @ApiResponse(code = 404, message = "Device not found for enrollment")
    })
    Response modifyEnrollment(@ApiParam(name = "id", value = "DeviceIdentifier") @PathParam("id") String id,
                              @ApiParam(name = "device", value = "Device information to be modify") Device device);

    @DELETE
    @Path("/{id}")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Un-registering an Android Device",
            notes = "Use this REST API to unregister a specific Android device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Device has removed successfully"),
            @ApiResponse(code = 404, message = "Device not found")
    })
    Response disEnrollDevice(@ApiParam(name = "id", value = "DeviceIdentifier") @PathParam("id") String id);

}
