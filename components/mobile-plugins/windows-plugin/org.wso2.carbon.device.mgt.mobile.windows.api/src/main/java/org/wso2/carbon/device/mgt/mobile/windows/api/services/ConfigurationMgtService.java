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

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Info;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.Tag;
import io.swagger.annotations.Api;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.Message;
import org.wso2.carbon.device.mgt.mobile.windows.api.operations.util.Constants;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Windows Platform Configuration REST-API implementation.
 * All end points supports JSON, XMl with content negotiation.
 */

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "Windows Configuration Management"),
                                @ExtensionProperty(name = "context",
                                        value = "/api/device-mgt/windows/v1.0/configuration"),
                        })
                }
        ),
        tags = {
                @Tag(name = "windows", description = "")
        }
)
@Api(value = "Windows Configuration Management",
        description = "This carries all the resources related to Windows configurations management functionality")
@WebService
@Path("/configuration")
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
@Scopes(
        scopes = {
                @Scope(
                        name = "Enroll Device",
                        description = "Register an Windows device",
                        key = "perm:windows:enroll",
                        permissions = {"/device-mgt/devices/enroll/windows"}
                ),
                @Scope(
                        name = "View Configurations",
                        description = "Getting Windows Platform Configurations",
                        key = "perm:windows:view-configuration",
                        permissions = {"/device-mgt/platform-configurations/view"}
                ),
                @Scope(
                        name = "Manage Configurations",
                        description = "Updating Windows Platform Configurations",
                        key = "perm:windows:manage-configuration",
                        permissions = {"/device-mgt/platform-configurations/manage"}
                )
        }
)
public interface ConfigurationMgtService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Windows Platform Configurations",
            notes = "Get the Windows platform configuration details using this REST API.",
            response = PlatformConfiguration.class,
            tags = "Windows Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:windows:view-configuration")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched Windows platform configuration.",
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
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Applications can be blacklisted via the application restriction policy too.."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n Th resource to be deleted does not exist."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the Windows platform configuration.")
    })
    Response getConfiguration(@ApiParam(
            name = "If-Modified-Since",
            value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                    "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                    "Example: Mon, 05 Jan 2014 15:10:00 +0200",
            required = false) @HeaderParam("If-Modified-Since") String ifModifiedSince);

    /**
     * Update Tenant Configurations for the specific Device type.
     *
     * @param configuration Tenant configurations to be updated.
     * @return Response message.
     * @throws WindowsConfigurationException
     */
    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating Windows Platform Configurations",
            notes = "Update the Windows platform configurations using this REST API.",
            tags = "Windows Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:windows:manage-configuration")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully updated the Windows platform configurations.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the updated Windows platform configuration."),
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
                            "Server error occurred while modifying the Windows platform configurations.")
    })
    Response updateConfiguration
    (@ApiParam(
            name = "configuration",
            value = "The properties to update the Windows platform configurations.")
     PlatformConfiguration configuration) throws WindowsConfigurationException;

    @GET
    @Path("license")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(
            produces = MediaType.TEXT_PLAIN,
            httpMethod = "GET",
            value = "Getting the License Agreement to Register a Windows Device",
            notes = "Use this REST API to retrieve the license agreement that is used for the Windows device " +
                    "registration process.",
            response = String.class,
            tags = "Windows Configuration Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:windows:enroll")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched Windows license configuration.",
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
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the Windows license configuration.")
    })
    Response getLicense(
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200.",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince) throws WindowsConfigurationException;

}
