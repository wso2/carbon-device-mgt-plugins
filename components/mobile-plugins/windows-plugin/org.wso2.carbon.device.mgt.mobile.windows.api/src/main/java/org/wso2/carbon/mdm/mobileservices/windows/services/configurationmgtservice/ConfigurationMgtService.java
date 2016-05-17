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

package org.wso2.carbon.mdm.mobileservices.windows.services.configurationmgtservice;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.mdm.mobileservices.windows.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.mdm.mobileservices.windows.common.util.Message;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Windows Platform Configuration REST-API implementation.
 * All end points supports JSON, XMl with content negotiation.
 */
@WebService
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface ConfigurationMgtService {

    /**
     * Save Tenant configurations.
     *
     * @param configuration Tenant Configurations to be saved.
     * @return Message type object for the provide save status.
     * @throws WindowsConfigurationException
     */
    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Configuring Windows Platform Settings",
            notes = "Configure the Windows platform settings using this REST API"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Windows platform configuration saved successfully"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    Message ConfigureSettings(TenantConfiguration configuration) throws WindowsConfigurationException;

    /**
     * Retrieve Tenant configurations according to the device type.
     *
     * @return Tenant configuration object contains specific tenant configurations.
     * @throws WindowsConfigurationException
     */
    @GET
    @ApiOperation(
            httpMethod = "GET",
            value = "Getting Windows Platform Configurations",
            notes = "Get the Windows platform configuration details using this REST API",
            response = TenantConfiguration.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Get Windows Configurations"),
            @ApiResponse(code = 500, message = "Server Error")
    })
    TenantConfiguration getConfiguration() throws WindowsConfigurationException;

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
            notes = "Update the Windows platform configurations using this REST API"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Error occurred while modifying configuration settings of " +
                    "windows platform")
    })
    Message updateConfiguration(TenantConfiguration configuration) throws WindowsConfigurationException;
}
