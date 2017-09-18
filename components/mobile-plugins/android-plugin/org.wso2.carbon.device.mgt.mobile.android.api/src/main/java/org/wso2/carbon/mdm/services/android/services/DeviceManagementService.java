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

import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.mdm.services.android.bean.wrapper.AndroidApplication;
import org.wso2.carbon.mdm.services.android.bean.wrapper.AndroidDevice;
import org.wso2.carbon.mdm.services.android.util.AndroidConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "Android Device Management"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/android/v1.0/devices"),
                        })
                }
        ),
        tags = {
                @Tag(name = "android,device_management", description = "")
        }
)
@Api(value = "Android Device Management",
        description = "This carries all the resources related to the Android device management functionalities.")
@Path("/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Scopes(
        scopes = {
                @Scope(
                        name = "Enroll Device",
                        description = "Register an Android device",
                        key = "perm:android:enroll",
                        permissions = {"/device-mgt/devices/enroll/android"}
                ),
                @Scope(
                        name = "Un-enroll Device",
                        description = "Unregister an Android device",
                        key = "perm:android:disenroll",
                        permissions = {"/device-mgt/devices/disenroll/android"}
                )
        }
)
public interface DeviceManagementService {

    @PUT
    @Path("/{id}/applications")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating the Application Details on Android Devices",
            notes = "Update the details of the applications that are installed on Android devices.",
            tags = "Android Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidConstants.SCOPE, value = "perm:android:enroll")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully updated the application details.",
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
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while updating the application list.")
    })
    Response updateApplicationList(
            @ApiParam(
                    name = "id",
                    value = "The unique device identifier.")
            @NotNull
            @Size(min = 2 , max = 45)
            @Pattern(regexp = "^[A-Za-z0-9]*$")
            @PathParam("id") String id,
            @ApiParam(
                    name = "applications",
                    value = "The properties to update application details. Multiple applications can be updated using comma separated values.")
            List<AndroidApplication> androidApplications);

    @PUT
    @Path("/{id}/pending-operations")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Getting the Pending Operation List",
            responseContainer = "List",
            notes = "The Android agent communicates with the server to get the operations that are queued up " +
                    "at the server end via this REST API." +
                    " While getting the pending operations the details of the operations executed at the device end is shared with the server. " +
                    "The server then updates the status of the operations that were carried out on the device.",
            response = Operation.class,
            tags = "Android Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidConstants.SCOPE, value = "perm:android:enroll")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the pending operations of the Android device.",
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
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests.")
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the list of pending operations.")
    })
    Response getPendingOperations(
            @ApiParam(
                    name = "id",
                    value = "The unique device identifier.",
                    required = true)
            @PathParam("id") String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200.",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @ApiParam(
                    name = "resultOperations",
                    value = "Properties to update the device operations and their status.")
            List<? extends Operation> resultOperations);

    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Registering an Android Device",
            notes = "When registering an Android device, you need to install" +
                    " the Android Agent on the device, before being able to register the device with WSO2 EMM. Instead," +
                    " you can use this REST API to register an Android device with WSO2 EMM, without having to install" +
                    " an Android Agent. This API can be mainly used to test the device enrollment process.",
            tags = "Android Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidConstants.SCOPE, value = "perm:android:enroll")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Successfully created an instance of the device.",
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
                            "Server error occurred while adding a new device.")
    })
    Response enrollDevice(@ApiParam(
            name = "device",
            value = "The properties required to enroll a device.")
                          @Valid AndroidDevice device);

    @GET
    @Path("/{id}/status")
    @ApiOperation(
            httpMethod = "GET",
            value = "Getting the Registration Status of an Android Device",
            notes = "Use this REST API to retrieve the registration status of an Android device.",
            tags = "Android Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidConstants.SCOPE, value = "perm:android:enroll")
                    })
            }
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
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the enrollment status of the Android device.")
    })
    Response isEnrolled(
            @ApiParam(
                    name = "id",
                    value = "The unique device identifier")
            @PathParam("id") String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @PUT
    @Path("/{id}")
    @ApiOperation(
            httpMethod = "PUT",
            value = "Updating the Registration Details of an Android Device",
            notes = "Use this REST API to update the registration details of an Android device.",
            tags = "Android Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidConstants.SCOPE, value = "perm:android:enroll")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated the device enrollment details.",
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified resource does not exist."),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The format of the requested entity was not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while updating the device enrollment.")
            })
    Response modifyEnrollment(
            @ApiParam(
                    name = "id",
                    value = "The unique device identifier.")
            @PathParam("id") String id,
            @ApiParam(
                    name = "device",
                    value = "The properties of required to update device enrollment details.") @Valid AndroidDevice androidDevice);

    @DELETE
    @Path("/{id}")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Unregistering an Android Device",
            notes = "Use this REST API to unregister an Android device.",
            tags = "Android Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidConstants.SCOPE, value = "perm:android:disenroll")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully disenrolled the device."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while dis-enrolling the device.")
    })
    Response disEnrollDevice(
            @ApiParam(
                    name = "id",
                    value = "The unique device identifier.")
            @PathParam("id") String id);

}
