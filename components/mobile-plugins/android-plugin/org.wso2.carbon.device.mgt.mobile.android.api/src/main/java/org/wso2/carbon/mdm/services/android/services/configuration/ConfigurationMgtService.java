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

package org.wso2.carbon.mdm.services.android.services.configuration;

import io.swagger.annotations.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.mdm.services.android.exception.AndroidAgentException;
import org.wso2.carbon.mdm.services.android.util.Message;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Android Platform Configuration REST-API implementation.
 * All end points supports JSON, XMl with content negotiation.
 */
@Api(value = "ConfigurationMgtService")
@WebService
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface ConfigurationMgtService {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Configuring Android Platform Settings",
            notes = "Configure the Android platform settings using this REST API"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Android platform configuration saved successfully"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    Message configureSettings(@ApiParam(name = "configuration", value = "AndroidPlatformConfiguration")
                                      TenantConfiguration configuration) throws AndroidAgentException;

    @GET
    @ApiOperation(
            httpMethod = "GET",
            value = "Getting Android Platform Configurations",
            notes = "Get the Android platform configuration details using this REST API",
            response = TenantConfiguration.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get Android Configurations"),
            @ApiResponse(code = 500, message = "Server Error")
    })
    TenantConfiguration getConfiguration() throws AndroidAgentException;

    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating Android Platform Configurations",
            notes = "Update the Android platform configurations using this REST API"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Error occurred while modifying configuration settings of " +
                    "Android platform")
    })
    Message updateConfiguration(TenantConfiguration configuration) throws AndroidAgentException;
}
