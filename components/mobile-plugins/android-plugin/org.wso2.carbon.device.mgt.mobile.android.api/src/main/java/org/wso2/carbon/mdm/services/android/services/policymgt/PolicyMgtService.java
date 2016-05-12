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

package org.wso2.carbon.mdm.services.android.services.policymgt;

import io.swagger.annotations.*;
import org.wso2.carbon.mdm.services.android.exception.AndroidAgentException;
import org.wso2.carbon.mdm.services.android.util.Message;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Api("PolicyMgtService")
@WebService
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface PolicyMgtService {

    @GET
    @Path("{deviceId}")
    @ApiOperation(
            httpMethod = "GET",
            value = "Identifying whether a Policy is Enforced on an Android Device",
            notes = "When a device registers with WSO2 EMM, a policy is enforced on the device based on the policy " +
                    "enforcement criteria. Using this API you are able to identify if a specific device has a policy " +
                    "enforced or if no policy is enforced on the device."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Effective policy added to operation"),
            @ApiResponse(code = 204, message = "No effective policy found")
    })
    Message getEffectivePolicy(@ApiParam(name = "acceptHeader", value = "Accept Header") @HeaderParam("Accept")
                                       String acceptHeader,
                               @ApiParam(name = "deviceId", value = "DeviceIdentifier") @PathParam("deviceId")
                                       String id) throws AndroidAgentException;

    @GET
    @Path("/features/{deviceId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get Effective Features",
            responseContainer = "List",
            notes = "Get already applied features for given device Identifier",
            response = ProfileFeature.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Effective Feature List"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 500, message = "Error occurred while getting the features")
    })
    List<ProfileFeature> getEffectiveFeatures(@ApiParam(name = "acceptHeader", value = "Accept Header")
                                              @HeaderParam("Accept") String acceptHeader,
                                              @ApiParam(name = "deviceId", value = "DeviceIdentifier")
                                              @PathParam("deviceId") String id) throws AndroidAgentException;
}
