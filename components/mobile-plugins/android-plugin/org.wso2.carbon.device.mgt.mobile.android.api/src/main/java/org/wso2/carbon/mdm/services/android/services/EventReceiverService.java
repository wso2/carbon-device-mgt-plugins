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

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Info;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.Tag;
import io.swagger.annotations.Api;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.wso2.carbon.mdm.services.android.bean.DeviceState;
import org.wso2.carbon.mdm.services.android.bean.wrapper.EventBeanWrapper;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "Android Event Receiver"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/android/v1.0/events"),
                        })
                }
        ),
        tags = {
                @Tag(name = "android", description = "")
        }
)
@Api(value = "Event Receiver", description = "Event publishing/retrieving related APIs. To enable event publishing/retrieving you need to" +
        " configure WSO2 EMM as explained in https://docs.wso2.com/display/EMM220/Managing+Event+Publishing+with+WSO2+Data+Analytics+Server, " +
        "https://docs.wso2.com/display/EMM220/Creating+a+New+Event+Stream+and+Receiver")
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
            value = "Publishing Events",
            notes = "Publish events received by the WSO2 EMM Android client to the WSO2 Data Analytics Server (DAS) using this API.",
            tags = "Event Receiver",
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "/device-mgt/devices/enroll/android",
                                    description = "Publish Events to DAS") }
                    )
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 201, message = "Created. \n Successfully published the event. Location header " +
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests.")
                            }),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n The source can be retrieved from the URL specified in the location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error."),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The format of the requested entity was not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while publishing events.")
            })
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
            notes = "Get the event details of a device for a given time duration using this API. The request must contain " +
                    "the device identifier. Optionally, both date from and date to value should be present to get " +
                    "alerts between a specified time. Filtering can also be done based on the device type and the device identifier." +
                    "(This cannot be combined with the to and from parameters).",
            response = DeviceState.class,
            responseContainer = "List",
            tags = "Event Receiver",
            authorizations = {
                    @Authorization(
                            value="permission",
                            scopes = { @AuthorizationScope(scope = "/device-mgt/devices/enroll/android",
                                    description = "Publish Events to DAS") }
                    )
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the event details of the specified device for a given time duration.",
                            response = DeviceState.class, responseContainer = "List"),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n The source can be retrieved from the URL specified in the location header.",
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
                                    " the device identifier. Additionally, the device identifier can be combined with either the device type" +
                                    " OR the from and to date."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified resource does not exist."),
                    @ApiResponse(
                            code = 500,
                            message = "Error occurred while getting the published events for the specified device.")
            })
    Response retrieveAlerts(
            @ApiParam(
                    name = "id",
                    value = "The unique device identifier.",
                    required = true)
            @Size(min = 2, max = 45)
            @QueryParam("id") String deviceId,
            @ApiParam(
                    name = "from",
                    value = "Specify the time and date from when you want to get the data." +
                            "Provide the time and date format in the unix/epoch format as the value for {unixTimestamp}. Use a Epoch convertor, to convert the time and date to this format.")
            @QueryParam("from") long from,
            @ApiParam(
                    name = "to",
                    value = "Specify the time and date up to when you require the data." +
                            "Provide the time and date format in the unix/epoch format as the value for {unixTimestamp}. Use a Epoch convertor, to convert the time and date to this format.")
            @QueryParam("to") long to,
            @ApiParam(
                    name = "type",
                    value = "The alert type to retrieve the events." +
                            "Provide APPLICATION_STATE or RUNTIME_STATE as the value.")
            @Size(min = 2, max = 45)
            @QueryParam("type") String type,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

}
