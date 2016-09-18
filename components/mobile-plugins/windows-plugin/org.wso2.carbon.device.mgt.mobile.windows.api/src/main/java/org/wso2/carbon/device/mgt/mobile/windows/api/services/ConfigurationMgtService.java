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

import io.swagger.annotations.Api;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.Message;

import javax.jws.WebService;
import javax.ws.rs.*;

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

    /**
     * Save Tenant configurations.
     *
     * @param configuration Tenant Configurations to be saved.
     * @return Message type object for the provide save status.
     * @throws WindowsConfigurationException
     */
    @POST
    @Permission(name = "Manage Configurations", permission = "/device-mgt/platform-configurations/manage")
    Message ConfigureSettings(PlatformConfiguration configuration) throws WindowsConfigurationException;

    /**
     * Retrieve Tenant configurations according to the device type.
     *
     * @return Tenant configuration object contains specific tenant configurations.
     * @throws WindowsConfigurationException
     */
    @GET
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
    @Permission(name = "Manage Configurations", permission = "/device-mgt/platform-configurations/manage")
    Message updateConfiguration(PlatformConfiguration configuration) throws WindowsConfigurationException;
}
