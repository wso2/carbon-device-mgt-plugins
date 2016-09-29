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
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.Message;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Windows Platform Configuration REST-API implementation.
 * All end points supports JSON, XMl with content negotiation.
 */
@API(name = "Windows Configuration Management", version = "1.0.0",
     context = "api/device-mgt/windows/v1.0/services/configuration",
     tags = {"windows"})
@Api(value = "Windows Configuration Management",
     description = "This carries all the resources related to Windows configurations management functionalities")
@WebService
@Path("services/configuration")
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface ConfigurationMgtService {
    
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Windows Platform Configurations",
            notes = "Get the Windows platform configuration details using this REST API",
            response = PlatformConfiguration.class,
            tags = "Windows Configuration Management"
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n Resource to be deleted does not exist."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching Windows platform configuration.")
    })
    @Permission(name = "View Configurations", permission = "/device-mgt/platform-configurations/view")
    PlatformConfiguration getConfiguration() throws WindowsConfigurationException;

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
            value = "Updating Windows Platform Configuration.",
            notes = "Update the Windows platform configurations using this REST API.",
            tags = "Windows Configuration Management"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Windows platform configuration has been updated successfully",
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
                            "Server error occurred while modifying Windows platform configuration.")
    })
    @Permission(name = "Manage Configurations", permission = "/device-mgt/configurations/manage")
    Message updateConfiguration( @ApiParam(name = "configuration",
            value = "PlatformConfiguration")PlatformConfiguration configuration) throws WindowsConfigurationException;

    @GET
    @Path("license")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(
            produces = MediaType.TEXT_PLAIN,
            httpMethod = "GET",
            value = "Getting the License Agreement for Windows Device Registration",
            notes = "Use this REST API to retrieve the license agreement that is used for the Windows device " +
                    "registration process",
            response = String.class,
            tags = "Windows Configuration Management")
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
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n Resource to be deleted does not exist."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching Windows license configuration.")
    })
    @Permission(name = "Enroll Device", permission = "/device-mgt/devices/enroll/windows")
    Response getLicense(
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince) throws WindowsConfigurationException;

}
