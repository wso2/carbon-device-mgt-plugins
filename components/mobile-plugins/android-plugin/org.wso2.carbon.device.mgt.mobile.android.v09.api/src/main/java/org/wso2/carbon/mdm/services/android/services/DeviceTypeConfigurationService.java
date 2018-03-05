/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.mdm.services.android.services;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Info;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.Tag;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.mdm.services.android.bean.AndroidPlatformConfiguration;
import org.wso2.carbon.mdm.services.android.exception.AndroidAgentException;
import org.wso2.carbon.mdm.services.android.util.AndroidConstants;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "0.9.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name",
                                        value = "Android Configuration Management"),
                                @ExtensionProperty(name = "context",
                                        value = "/api/device-mgt/android/v0.9/configuration"),
                        })
                }
        ),
        tags = {
                @Tag(name = "android,device_management", description = "")
        }
)
@Api(value = "Android Configuration Management", description = "This API carries all the resource used to mange the Android platform configurations.")
@Path("/configuration")
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
                        name = "View Configurations",
                        description = "Getting Android Platform Configurations",
                        key = "perm:android:view-configuration",
                        permissions = {"/device-mgt/devices/enroll/android"}
                ),
                @Scope(
                        name = "Manage Configurations",
                        description = "Updating Android Platform Configurations",
                        key = "perm:android:manage-configuration",
                        permissions = {"/device-mgt/platform-configurations/manage"}
                )
        }
)
public interface DeviceTypeConfigurationService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Android Platform Configurations",
            notes = "Get the Android platform configuration details using this REST API.",
            response = PlatformConfiguration.class,
            tags = "Android Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidConstants.SCOPE, value = "perm:android:view-configuration")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the Android platform configurations.",
                    response = PlatformConfiguration.class,
                    responseHeaders = {
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
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the Android platform configuration.")
    })
    Response getConfiguration(
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating Android Platform Configurations",
            notes = "Update the Android platform configurations using this REST API.",
            tags = "Android Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidConstants.SCOPE, value = "perm:android:manage-configuration")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully updated the Android platform configurations.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the updated Android platform configuration."),
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
                            "Server error occurred while modifying the Android platform configuration.")
    })
    Response updateConfiguration(
            @ApiParam(name = "configuration",
                    value = "The properties to update the Android platform configurations.")
            @Valid AndroidPlatformConfiguration androidPlatformConfiguration);

    @GET
    @Path("license")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(
            produces = MediaType.TEXT_PLAIN,
            httpMethod = "GET",
            value = "Getting the License Agreement for the Android Device Registration",
            notes = "Use this REST API to retrieve the license agreement that is used for the Android device " +
                    "registration process.",
            response = String.class,
            tags = "Android Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidConstants.SCOPE, value = "perm:android:enroll")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched Android license configuration.",
                    response = String.class,
                    responseHeaders = {
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
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the Android license configuration.")
    })
    Response getLicense(
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200.",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince) throws AndroidAgentException;

}
