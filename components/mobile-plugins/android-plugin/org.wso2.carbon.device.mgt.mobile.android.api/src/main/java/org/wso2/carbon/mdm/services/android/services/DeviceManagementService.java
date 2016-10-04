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
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.mdm.services.android.bean.wrapper.AndroidApplication;
import org.wso2.carbon.mdm.services.android.bean.wrapper.AndroidDevice;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@API(name = "Android Device Management", version = "1.0.0",
        context = "api/device-mgt/android/v1.0/devices",
        tags = {"devicemgt_android"})

@Api(value = "Android Device Management",
        description = "This carries all the resources related to Android device management functionalities")
@Path("/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceManagementService {

    @PUT
    @Path("/{id}/applications")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update the application list of a device",
            tags = "Android Device Management"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Application list has been updated successfully",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The URL of the updated application list."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n Resource to be deleted does not exist."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while updating the application list.")
    })
    @Permission(name = "Enroll Device", permission = "/device-mgt/devices/enroll/android")
    Response updateApplicationList(
            @ApiParam(
                    name = "id",
                    value = "Device Identifier")
            @NotNull
            @Size(min = 2 , max = 45)
            @Pattern(regexp = "^[A-Za-z0-9]*$")
            @PathParam("id") String id,
            @ApiParam(
                    name = "applications",
                    value = "List of applications that need to be persisted against the device")
            List<AndroidApplication> androidApplications);

    @PUT
    @Path("/{id}/pending-operations")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Get the operation list pending for the device",
            responseContainer = "List",
            notes = "The Android agent communicates with the server to get the operations that are queued up " +
                    "at the server end for a given device using via this particular resource",
            response = Operation.class,
            tags = "Android Device Management")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the pending application list of the Android device.",
                    response = Operation.class,
                    responseContainer = "List",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of the requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching policies.")
    })
    @Permission(name = "Enroll Device", permission = "/device-mgt/devices/enroll/android")
    Response getPendingOperations(
            @ApiParam(
                    name = "id",
                    value = "Device Identifier")
            @PathParam("id") String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @ApiParam(
                    name = "resultOperations",
                    value = "Device Operation Status")
            List<? extends Operation> resultOperations);

    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Register an Android Device",
            notes = "When carrying out device registration via an Android device, you need to initially install" +
                    " an Android Agent on the device, before being able to register the device with WSO2 EMM. Instead," +
                    " you can use this REST API to register an Android device with WSO2 EMM, without having to install" +
                    " an Android Agent on the respective device",
            tags = "Android Device Management"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Device enrollment has successfully been created",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the added device enrollment."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new device.")
    })
    @Permission(name = "Enroll Device", permission = "/device-mgt/devices/enroll/android")
    Response enrollDevice(@ApiParam(name = "device", value = "Device Information to be enroll")
                          @Valid AndroidDevice device);

    @GET
    @Path("/{id}/status")
    @ApiOperation(
            httpMethod = "GET",
            value = "Getting the Registration Status of an Android Device",
            notes = "Use this REST API to retrieve the registration status of an Android device",
            tags = "Android Device Management"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the status of the Android device enrollment.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of the requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the enrollment status of the Android device.")
    })
    @Permission(name = "Enroll Device", permission = "/device-mgt/devices/enroll/android")
    Response isEnrolled(
            @ApiParam(
                    name = "id",
                    value = "Device Identifier")
            @PathParam("id") String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @PUT
    @Path("/{id}")
    @ApiOperation(
            httpMethod = "PUT",
            value = "Updating the Registration Details of an Android Device",
            notes = "Use this REST API to update the registration details of an Android device",
            tags = "Android Device Management"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Device enrollment has been updated successfully",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "URL of the updated device enrollment."),
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "Content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Resource to be deleted does not exist."),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while updating the device enrollment.")
            })
    @Permission(name = "Enroll Device", permission = "/device-mgt/devices/enroll/android")
    Response modifyEnrollment(
            @ApiParam(
                    name = "id",
                    value = "Device Identifier")
            @PathParam("id") String id,
            @ApiParam(
                    name = "device",
                    value = "Device information to be modify") @Valid AndroidDevice androidDevice);

    @DELETE
    @Path("/{id}")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Un-register an Android Device",
            notes = "Use this REST API to un-register a specific Android device",
            tags = "Android Device Management"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Device has successfully been dis-enrolled"),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n Resource to be deleted does not exist."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while dis-enrolling the device.")
    })
    @Permission(name = "Disenroll Device", permission = "/device-mgt/devices/disenroll/android")
    Response disEnrollDevice(
            @ApiParam(
                    name = "id",
                    value = "Device Identifier")
            @PathParam("id") String id);

}
