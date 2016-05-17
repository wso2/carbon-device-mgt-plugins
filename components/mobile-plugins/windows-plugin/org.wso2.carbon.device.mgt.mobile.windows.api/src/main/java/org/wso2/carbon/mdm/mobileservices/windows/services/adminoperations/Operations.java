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

package org.wso2.carbon.mdm.mobileservices.windows.services.adminoperations;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.mdm.mobileservices.windows.common.exceptions.WindowsDeviceEnrolmentException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Interface for Admin operations persisting. This interface accepts operations added via UI.
 */

@Api(value = "Operations", description = "Windows Device Management REST-API implementation.")
@Path("/operation")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

public interface Operations {

    @POST
    @Path("/devicelock")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a Device Lock on Windows Devices via the REST API",
            notes = "Adding a Device lock operation to the windows device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Internal Server error")
    })
    Response lock(@HeaderParam("Accept") String headerParam, List<String> deviceids) throws WindowsDeviceEnrolmentException;

    @POST
    @Path("/devicedisenroll")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Disenrolling Windows Devices via the REST API",
            notes = "Enforcing Disenrolling operation to the windows device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Internal Server error")
    })
    Response disenroll(@HeaderParam("Accept") String headerParam, List<String> deviceids) throws WindowsDeviceEnrolmentException;

    @POST
    @Path("/devicewipe")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Wiping Information off Windows Devices via the REST API",
            notes = "Enforce wipe operation to the windows device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Internal Server error")
    })
    Response wipe(@HeaderParam("Accept") String headerParam, List<String> deviceids) throws WindowsDeviceEnrolmentException;

    @POST
    @Path("/devicering")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Ringing Windows Devices via the Rest API",
            notes = "Adding a Device ring operation to the windows device."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Internal Server error")
    })
    Response ring(@HeaderParam("Accept") String headerParam, List<String> deviceids) throws WindowsDeviceEnrolmentException;

    @POST
    @Path("/lockreset")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Resetting the Lock of Windows Devices via the REST API",
            notes = "Adding a Device lock re-test operation to the windows device."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Internal Server error")
    })
    Response lockReset(@HeaderParam("Accept") String acceptHeader, List<String> deviceids)
            throws WindowsDeviceEnrolmentException;
}
