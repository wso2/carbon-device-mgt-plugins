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

package org.wso2.carbon.device.mgt.mobile.windows.api.services;

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsDeviceEnrolmentException;

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Interface for Admin operations persisting. This interface accepts operations added via UI.
 */


@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name",
                                        value = "Windows Device Management Administrative Service"),
                                @ExtensionProperty(name = "context",
                                        value = "/api/device-mgt/windows/v1.0/admin/devices"),
                        })
                }
        ),
        tags = {
                @Tag(name = "devicemgt_windows", description = "")
        }
)
@Api(value = "Windows Device Management Administrative Service",
        description = "Device management related admin APIs.")
@WebService
@Path("/admin/devices")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface DeviceManagementAdminService {

    @POST
    @Path("/lock-devices")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a Device Lock on Windows devices.",
            notes = "Using this API you have the option of Device Windows device.",
            response = Activity.class,
            tags = "Windows Device Management Administrative Service",
            authorizations = {
                    @Authorization(
                            value = "permission",
                            scopes = {@AuthorizationScope(
                                    scope = "/device-mgt/devices/owning-device/operations/windows/lock",
                                    description = "Lock Device")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Successfully scheduled the device lock operation.",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified. \n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n The source can be retrieved from the URL specified in the location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.\n"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while locking the device.")
    })
    Response lock(@HeaderParam("Accept") String headerParam, @ApiParam(
            name = "deviceIDs",
            value = "Provide the ID of the AWindows device. Multiple device IDs can be added by " +
                    "using comma separated values. ",
            required = true) List<String> deviceIds) throws WindowsDeviceEnrolmentException;

    @POST
    @Path("/disenroll-devices")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Dis-enrol the windows Devices",
            notes = "Dis-enroll on Android devices",
            response = Activity.class,
            tags = "Windows Device Management Administrative Service.",
            authorizations = {
                    @Authorization(
                            value = "permission",
                            scopes = {@AuthorizationScope(
                                    scope = "/device-mgt/devices/disenroll/windows",
                                    description = "Dis-enroll the windows devices ")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Successfully scheduled the Dis-enroll operation.",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n The source can be retrieved from the URL specified in the location header.\n",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a Dis-enroll operation.")
    })
    Response disenroll(@HeaderParam("Accept") String headerParam, @ApiParam(
            name = "deviceIDs",
            value = "Provide the ID of the A Windows device. Multiple device IDs can be added by " +
                    "using comma separated values. ",
            required = true) List<String> deviceIds) throws WindowsDeviceEnrolmentException;

    @POST
    @Path("/wipe-devices")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Factory Resetting an Windows Device",
            notes = "Factory rest or erase all the data stored on the Windows devices" +
                    "to restore them back to the original system.",
            response = Activity.class,
            tags = "Windows Device Management Administrative Service",
            authorizations = {
                    @Authorization(
                            value = "permission",
                            scopes = {@AuthorizationScope(
                                    scope = "/device-mgt/devices/owning-device/operations/windows/wipe",
                                    description = "DeviceWipe")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Successfully scheduled the Data wipe operation.",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified." +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n The source can be retrieved from the URL specified in the location header.\n",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding the Data wipe operation.")})
    Response wipe(@HeaderParam("Accept") String headerParam, @ApiParam(
            name = "deviceIDs",
            value = "Provide the ID of the A Windows device. Multiple device IDs can be added by " +
                    "using comma separated values. ",
            required = true) List<String> deviceIds) throws WindowsDeviceEnrolmentException;

    @POST
    @Path("/ring-devices")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Ringing Windows Devices",
            notes = "Ring Windows devices.",
            response = Activity.class,
            tags = "Windows Device Management Administrative Service",
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(
                                    scope = "/device-mgt/devices/owning-device/operations/windows/ring",
                                    description = "Ring Device") }
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Successfully scheduled the device ring operation.",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n The source can be retrieved from the URL specified in the location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.\n"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new device ring operation.")
    })
    Response ring(@HeaderParam("Accept") String headerParam, @ApiParam(
            name = "deviceIDs",
            value = "Provide the ID of the A Windows device. Multiple device IDs can be added by " +
                    "using comma separated values. ",
            required = true) List<String> deviceIds) throws WindowsDeviceEnrolmentException;

    @POST
    @Path("/lock-reset-devices")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Lock reset on Windows devices",
            notes = "Lock reset on Windows devices.Its use to reset the device pass code",
            response = Activity.class,
            tags = "Windows Device Management Administrative Service",
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(
                                    scope = "/device-mgt/devices/owning-device/operations/windows/lock-reset",
                                    description = "Lock reset") }
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Successfully scheduled the lock-reset operation.",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n The source can be retrieved from the URL specified in the location header.\n",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding adding a lock-reset operation.")
    })
    Response lockReset(@HeaderParam("Accept") String acceptHeader, @ApiParam(
            name = "deviceIDs",
            value = "Provide the ID of the A Windows device. Multiple device IDs can be added by " +
                    "using comma separated values. ",
            required = true)  List<String> deviceIds) throws WindowsDeviceEnrolmentException;
}
