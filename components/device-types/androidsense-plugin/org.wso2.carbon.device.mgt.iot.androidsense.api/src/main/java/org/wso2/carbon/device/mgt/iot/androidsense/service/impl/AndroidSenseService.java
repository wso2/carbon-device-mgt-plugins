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


package org.wso2.carbon.device.mgt.iot.androidsense.service.impl;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.ResponseHeader;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "android_sense"),
                                @ExtensionProperty(name = "context", value = "/android_sense"),
                        })
                }
        ),
        tags = {
                @Tag(name = "android_sense", description = "")
        }
)
public interface AndroidSenseService {

    /**
     * End point to send the key words to the device
     *
     * @param deviceId The registered device Id.
     * @param keywords The key words to be sent. (Comma separated values)
     */
    @POST
    @Path("device/{deviceId}/words")
    @ApiOperation(
            httpMethod = "POST",
            value = "End point to send the key words to the device",
            notes = "",
            tags = "android_sense",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "", value = "perm:android-sense:enroll")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error occurred while executing command operation to"
                            + " send keywords",
                    response = Response.class)
    })
    Response sendKeyWords(
            @ApiParam(
                    name = "deviceId",
                    value = "The registered device Id.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "keywords",
                    value = "The key words to be sent. (Comma separated values)",
                    required = true)
            @QueryParam("keywords") String keywords);

    /**
     * End point to send the key words to the device
     *
     * @param deviceId  The registered device Id.
     * @param threshold The key words to be sent. (Comma separated values)
     */
    @POST
    @Path("device/{deviceId}/words/threshold")
    @ApiOperation(
            httpMethod = "POST",
            value = "End point to send threshold value to the device",
            notes = "",
            tags = "android_sense",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "", value = "perm:android-sense:enroll")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error occurred while executing command operation to"
                            + " send threashold",
                    response = Response.class)
    })
    Response sendThreshold(
            @ApiParam(
                    name = "deviceId",
                    value = "The registered device Id.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "threshold",
                    value = "Threshold to be sent.",
                    required = true)
            @QueryParam("threshold") String threshold);

    /**
     * End point to remove key words from the device
     */
    @DELETE
    @Path("device/{deviceId}/words")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Remove key words from the device",
            notes = "",
            response = Response.class,
            tags = "android_sense",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "", value = "perm:android-sense:enroll")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error occurred while executing command operation to"
                            + " send threashold",
                    response = Response.class)
    })
    Response removeKeyWords(
            @ApiParam(
                    name = "deviceId",
                    value = "The registered device Id.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "words",
                    value = "The key words to be sent. (Comma separated values)",
                    required = true)
            @QueryParam("words") String words);

    /**
     * Retrieve Sensor data for the device type
     */
    @GET
    @Path("stats/{deviceId}/sensors/{sensorName}")
    @ApiOperation(
            consumes = "application/json",
            produces = "application/json",
            httpMethod = "GET",
            value = "Retrieve Sensor data for the device type",
            notes = "",
            response = Response.class,
            tags = "android_sense",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "", value = "perm:android-sense:enroll")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
                    response = Response.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getAndroidSenseDeviceStats(
            @ApiParam(
                    name = "deviceId",
                    value = "The registered device Id.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "sensorName",
                    value = "Name of the sensor",
                    required = true)
            @PathParam("sensorName") String sensor,
            @ApiParam(
                    name = "from",
                    value = "Get stats from what time",
                    required = true)
            @QueryParam("from") long from,
            @ApiParam(
                    name = "to",
                    value = "Get stats up to what time",
                    required = true)
            @QueryParam("to") long to);

    /**
     * Enroll devices.
     */
    @POST
    @Path("device/{device_id}/register")
    @ApiOperation(
            httpMethod = "POST",
            value = "Enroll device",
            notes = "",
            response = Response.class,
            tags = "android_sense",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "", value = "perm:android-sense:enroll")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 202,
                    message = "Accepted.",
                    response = Response.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response register(
            @ApiParam(
                    name = "deviceId",
                    value = "Device identifier id of the device to be added",
                    required = true)
            @PathParam("device_id") String deviceId,
            @ApiParam(
                    name = "deviceName",
                    value = "Device name of the device to be added",
                    required = true)
            @QueryParam("deviceName") String deviceName);

}

