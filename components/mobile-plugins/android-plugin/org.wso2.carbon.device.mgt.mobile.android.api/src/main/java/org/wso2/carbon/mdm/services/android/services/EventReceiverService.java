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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.mdm.services.android.bean.DeviceState;
import org.wso2.carbon.mdm.services.android.bean.wrapper.EventBeanWrapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface EventReceiverService {

    @POST
    @Path("/publish-event")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Event Publishing via REST API.",
            notes = "Publish events received by the WSO2 EMM Android client to WSO2 DAS using this API."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Event is published successfully."),
            @ApiResponse(code = 500, message = "Error occurred while publishing the events from Android agent.")
    })
    Response publishEvents(
            @ApiParam(name = "eventBeanWrapper",
                    value = "Information of the agent event to be published on DAS.")
            EventBeanWrapper eventBeanWrapper);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Event Details of a Device.",
            notes = "Get the event details received by an Android device using this API.",
            response = DeviceState.class,
            responseContainer = "List"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Device statuses."),
            @ApiResponse(code = 500,
                    message = "Error occurred while getting published events for specific given device.")
    })
    Response retrieveAlert(
            @ApiParam(name = "deviceId", value = "DeviceId which need to retrieve published events.")
            @QueryParam("id") String deviceId);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Event Details for a Given Time Period.",
            notes = "Get the event details of a device for a given time duration using this API.",
            response = DeviceState.class,
            responseContainer = "List"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "event details of a device for a given time duration"),
            @ApiResponse(code = 500, message = "Error occurred while getting published events for" +
                    " specific device on given Date.")
    })
    Response retrieveAlertFromDate(
            @ApiParam(name = "id",
                    value = "Device Identifier to be need to retrieve events.")
            @QueryParam("id") String deviceId,
            @ApiParam(name = "from", value = "From Date.")
            @QueryParam("from") long from,
            @ApiParam(name = "to", value = "To Date.")
            @QueryParam("to") long to);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieves event details by type",
            notes = "Get the event details of a device for a given time duration using this API.",
            response = DeviceState.class,
            responseContainer = "List"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "event details of a device for a given time duration"),
            @ApiResponse(code = 500, message = "Error occurred while getting published events for" +
                    " specific device on given Date.")
    })
    Response retrieveAlertByType(
            @ApiParam(name = "id", value = "Device Identifier to be need to retrieve events.")
            @QueryParam("id") String deviceId,
            @ApiParam(name = "type", value = "Type of the Alert to be need to retrieve events.")
            @QueryParam("type") String type);

}
