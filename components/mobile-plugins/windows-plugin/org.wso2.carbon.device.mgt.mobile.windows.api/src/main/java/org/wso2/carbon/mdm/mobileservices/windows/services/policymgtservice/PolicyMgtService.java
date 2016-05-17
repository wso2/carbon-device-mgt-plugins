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

package org.wso2.carbon.mdm.mobileservices.windows.services.policymgtservice;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.wso2.carbon.mdm.mobileservices.windows.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.mdm.mobileservices.windows.common.util.Message;

import javax.jws.WebService;
import javax.ws.rs.*;

/**
 * Endpoint for Enforce Effective Policy.
 */
@Api(value = "PolicyMgtService", description = "Windows Device Management REST-API implementation.")
@WebService
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface PolicyMgtService {
    @GET
    @Path("{id}")
    @ApiOperation(
            httpMethod = "GET",
            value = "Identifying whether a Policy is Enforced on an Windows Device",
            notes = "When a device registers with WSO2 EMM, a policy is enforced on the device based on the policy " +
                    "enforcement criteria. Using this API you are able to identify if a specific device has a policy " +
                    "enforced or if no policy is enforced on the device."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Effective policy added to operation"),
            @ApiResponse(code = 204, message = "No effective policy found")
    })
    Message getEffectivePolicy(@HeaderParam("Accept") String acceptHeader,
                               @PathParam("id") String id) throws WindowsConfigurationException;
}
