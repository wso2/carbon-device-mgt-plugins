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
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.mdm.services.android.bean.DeviceState;
import org.wso2.carbon.mdm.services.android.bean.wrapper.EventBeanWrapper;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@API(name = "Android Event Receiver", version = "1.0.0",
        context = "api/device-mgt/android/v1.0/events",
        tags = {"devicemgt_android"})

@Api(value = "Event Receiver", description = "Event publishing/retrieving related APIs.To enable Eventing need to" +
        " configure as ref-https://docs.wso2.com/display/EMM210/Managing+Event+Publishing+with+WSO2+Data+Analytics+Server, " +
        "https://docs.wso2.com/display/EMM210/Creating+a+New+Event+Stream+and+Receiver")
@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface EventReceiverService {

    @POST
    @Path("/publish")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Event publishing via REST API.",
            notes = "Publish events received by the EMM Android client to WSO2 DAS using this API.",
            tags = "Event Receiver"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 201, message = "Created. \n Event is published successfully. Location header " +
                            "contains URL of newly enrolled device",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The URL of the added policy."),
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests.")
                            }),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error."),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while publishing events.")
            })
    @Permission(name = "Publish Events to DAS", permission = "/device-mgt/devices/android/events/manage")
    Response publishEvents(
            @ApiParam(
                    name = "eventBeanWrapper",
                    value = "Information of the agent event to be published on DAS.")
                    @Valid
            EventBeanWrapper eventBeanWrapper);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting event details for a given time period.",
            notes = "Get the event details of a device for a given time duration using this API.Request must contain " +
                    "the device identifier. Optionally, both, date from and date to value should be present to get " +
                    "alerts between times. Based on device type and the device identifier also filtering can be done" +
                    "(This cannot be combined with to and from parameters).",
            response = DeviceState.class,
            responseContainer = "List",
            tags = "Event Receiver"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Event details of a device for a given time duration have been successfully fetched",
                            response = DeviceState.class, responseContainer = "List"),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n Source can be retrieved from the URL specified at" +
                                    " the Location header.",
                            responseHeaders = {
                                    @ResponseHeader(name = "Content-Location", description = "Source URL of the document.")
                            }),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error. You must provide" +
                                    " the device identifier. Additionally, the device identifier can be combined with either the type" +
                                    " OR date from and to."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Resource requested does not exist."),
                    @ApiResponse(
                            code = 500,
                            message = "Error occurred while getting published events for specific device.")
            })
    @Permission(name = "Publish Events to DAS", permission = "/device-mgt/devices/android/events/manage")
    Response retrieveAlerts(
            @ApiParam(
                    name = "id",
                    value = "Device Identifier to be need to retrieve events.",
                    required = true)
            @Size(min = 2, max = 45)
            @QueryParam("id") String deviceId,
            @ApiParam(
                    name = "from",
                    value = "From Date.")
            @QueryParam("from") long from,
            @ApiParam(
                    name = "to",
                    value = "To Date.")
            @QueryParam("to") long to,
            @ApiParam(
                    name = "type",
                    value = "Type of the Alert to be need to retrieve events.")
            @Size(min = 2, max = 45)
            @QueryParam("type") String type,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

}
