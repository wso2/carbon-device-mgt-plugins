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

import io.swagger.annotations.*;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(value = "Android Device Management API",
        description = "This carries all the resources related to Android device management functionalities")
@Path("/devices")
@Api(value = "DeviceManagement", description = "Device management related APIs that is used by mainly by the Android " +
        "agent.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceManagementService {

    @PUT
    @Path("/{id}/applications")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update the application list of a device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n " +
                    "Application list of the device has successfully been updated"),
            @ApiResponse(code = 204, message = "No Content. \n Application list of the device has not been updated"),
            @ApiResponse(code = 500, message = "Internal Server Error. \n " +
                    "Sever error occurred while modifying the application list")
    })
    Response updateApplicationList(@ApiParam(name = "id", value = "Device Identifier") @PathParam("id") String id,
                                   @ApiParam(name = "applications", value = "List of applications that need to be " +
                                           "persisted against the device")
                                   List<Application> applications);

    @GET
    @Path("/{id}/pending-operations")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Get the operation list pending for the device",
            responseContainer = "List",
            notes = "The Android agent communicates with the server to get the operations that are queued up " +
                    "at the server end for a given device using via this particular resource",
            response = Operation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n List of pending operations of the device is returned",
                    responseHeaders = {
                            @ResponseHeader(name = "Content-Type", description = "The content type of the body"),
                            @ResponseHeader(name = "ETag", description = "Entity Tag of the response resource.\n" +
                                    "Used by caches, or in conditional requests."),
                            @ResponseHeader(name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(code = 303, message = "See Other. \n " +
                    "Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(name = "Location", description = "The Source URL of the document.")}),
            @ApiResponse(code = 304, message = "Not Modified. \n " +
                    "Empty body because the client already has the latest version of the requested resource."),
            @ApiResponse(code = 400, message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(code = 404, message = "Not Found. \n Requested device not found."),
            @ApiResponse(code = 406, message = "Not Acceptable. \n The requested media type is not supported."),
            @ApiResponse(code = 500, message = "Internal Server Error. \n " +
                    "Server error occurred while retrieving the pending operation list of the device.")
    })
    Response getPendingOperations(
            @ApiParam(name = "id", value = "Device Identifier") @PathParam("id") String id,
            @ApiParam(name = "resultOperations", value = "Device Operation Status")
            List<? extends Operation> resultOperations);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Register an Android Device",
            notes = "When carrying out device registration via an Android device, you need to initially install" +
                    " an Android Agent on the device, before being able to register the device with WSO2 EMM. Instead," +
                    " you can use this REST API to register an Android device with WSO2 EMM, without having to install" +
                    " an Android Agent on the respective device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created. \n Device has successfully been enrolled. Location header " +
                    "contains URL of newly enrolled device",
                    responseHeaders = {
                            @ResponseHeader(name = "Location", description = "URL of the device enrolled")}),
            @ApiResponse(code = 400, message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(code = 500, message = "Internal Server Error. \n " +
                    "Server error occurred while enrolling the device."),
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
            @ApiResponse(code = 200, message = "Device has already enrolled",
                    responseHeaders = {
                            @ResponseHeader(name = "Content-Type", description = "The content type of the body"),
                            @ResponseHeader(name = "ETag", description = "Entity Tag of the response resource.\n" +
                                    "Used by caches, or in conditional requests."),
                            @ResponseHeader(name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(code = 404, message = "Not Found. \n Requested device not found."),
            @ApiResponse(code = 500, message = "Internal Server Error. \n " +
                    "Server error occurred while retrieving the enrollment status of the device."),
    })
    Response isEnrolled(@ApiParam(name = "id", value = "Device Identifier") @PathParam("id") String id);

    @PUT
    @Path("/{id}")
    @ApiOperation(
            httpMethod = "PUT",
            value = "Updating the Registration Details of an Android Device",
            notes = "Use this REST API to update the registration details of an Android device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Device enrollment has been updated successfully",
                    responseHeaders = {
                            @ResponseHeader(name = "Location", description = "The URL of the updated device."),
                            @ResponseHeader(name = "Content-Type", description = "The content type of the body"),
                            @ResponseHeader(name = "ETag", description = "Entity Tag of the response resource.\n" +
                                    "Used by caches, or in conditional requests."),
                            @ResponseHeader(name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(code = 204, message = "No Content. \n Enrollment of the device has not been updated"),
            @ApiResponse(code = 400, message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(code = 404, message = "Not Found. \n Resource to be deleted does not exist."),
            @ApiResponse(code = 500, message = "Internal Server Error. \n " +
                    "Server error occurred while modifying the current enrollment of the device.")
    })
    Response modifyEnrollment(@ApiParam(name = "id", value = "Device Identifier") @PathParam("id") String id,
                              @ApiParam(name = "device", value = "Device information to be modify") Device device);

    @DELETE
    @Path("/{id}")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Un-registering an Android Device",
            notes = "Use this REST API to un-register a specific Android device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Device has successfully been dis-enrolled"),
            @ApiResponse(code = 404, message = "Not Found. \n Device requested to be dis-enrolled does not exist."),
            @ApiResponse(code = 500, message = "Internal Server Error. \n " +
                    "Server error occurred while dis-enrolling the the device."),
    })
    Response disEnrollDevice(@ApiParam(name = "id", value = "Device Identifier") @PathParam("id") String id);

}
