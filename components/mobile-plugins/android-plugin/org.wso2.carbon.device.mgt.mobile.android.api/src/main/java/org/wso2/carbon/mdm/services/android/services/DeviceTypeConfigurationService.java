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
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.mdm.services.android.exception.AndroidAgentException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/configuration")

@Api(value = "DeviceTypeConfiguration", description = "Device type specific configuration APIs.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceTypeConfigurationService {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Configuring Android platform settings.",
            notes = "Configure the Android platform settings using this REST API."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created. \n Android platform configuration saved successfully",
                    responseHeaders = {
                        @ResponseHeader(name = "Location", description = "URL of the configuration added.")
                    }),
            @ApiResponse(code = 400, message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(code = 500, message = "Internal Server Error. \n" +
                    " Error occurred while saving configurations for the android platform.")
    })
    Response addConfiguration(@ApiParam(name = "configuration",
            value = "Android platform related configurations that must be added.")
            PlatformConfiguration configuration);
    @GET
    @ApiOperation(
            httpMethod = "GET",
            value = "Getting Android Platform Configurations",
            notes = "Get the Android platform configuration details using this REST API",
            response = PlatformConfiguration.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get Android Configuration."),
            @ApiResponse(code = 303, message = "See Other. \n " +
                    "Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(name = "Location", description = "The Source URL of the document.")}),
            @ApiResponse(code = 304, message = "Not Modified. \n " +
                    "Empty body because the client already has the latest version of the requested resource."),
            @ApiResponse(code = 404, message = "Not Found. \n Resource requested does not exist."),
            @ApiResponse(code = 500, message = "Internal Server Error. \n Error occurred while fetching " +
                    "configuration settings of Android platform.")
    })
    Response getConfiguration() throws AndroidAgentException;

    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating Android Platform Configuration.",
            notes = "Update the Android platform configurations using this REST API."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Created. \n Configurations was updated successfully."),
            @ApiResponse(code = 400, message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(code = 404, message = "Not Found. \n Resource to be changed does not exist."),
            @ApiResponse(code = 500, message = "Internal Server Error. \n Error occurred while modifying " +
                    "configuration settings of Android platform.")
    })
    Response updateConfiguration(@ApiParam(name = "configuration", value = "AndroidPlatformConfiguration")
                                 PlatformConfiguration configuration) throws AndroidAgentException;

    @GET
    @Path("license")
    @Produces("text/plain")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the License Agreement for Android Device Registration",
            notes = "Use this REST API to retrieve the license agreement that is used for the Android device " +
                    "registration process",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Licence agreement"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the license configured for Android " +
                    "device enrolment")
    })
    Response getLicense() throws AndroidAgentException;

}
