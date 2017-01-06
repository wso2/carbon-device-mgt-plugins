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

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.constants.AndroidSenseConstants;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
@Scopes(
        scopes = {
                @Scope(
                        name = "Enroll device",
                        description = "",
                        key = "perm:android-sense:enroll",
                        permissions = {"/device-mgt/devices/enroll/android-sense"}
                )
        }
)
public interface AndroidSenseService {

    /**
     * End point to send the key words to the device
     *
     * @param deviceId The registered device Id.
     * @param keywords The key words to be sent. (Comma separated values)
     */
    @Path("device/{deviceId}/words")
    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Send the key words to the device",
            notes = "",
            response = Response.class,
            tags = "android_sense",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidSenseConstants.SCOPE, value = "perm:android-sense:enroll")
                    })
            }
    )
    Response sendKeyWords(@PathParam("deviceId") String deviceId, @QueryParam("keywords") String keywords);

    /**
     * End point to send the key words to the device
     *
     * @param deviceId  The registered device Id.
     * @param threshold The key words to be sent. (Comma separated values)
     */
    @Path("device/{deviceId}/words/threshold")
    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Send threshold value to the device",
            notes = "",
            response = Response.class,
            tags = "android_sense",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidSenseConstants.SCOPE, value = "perm:android-sense:enroll")
                    })
            }
    )
    Response sendThreshold(@PathParam("deviceId") String deviceId, @QueryParam("threshold") String threshold);

    @Path("device/{deviceId}/words")
    @DELETE
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Remove key words from the device",
            notes = "",
            response = Response.class,
            tags = "android_sense",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidSenseConstants.SCOPE, value = "perm:android-sense:enroll")
                    })
            }
    )
    Response removeKeyWords(@PathParam("deviceId") String deviceId, @QueryParam("words") String words);

    /**
     * Retrieve Sensor data for the device type
     */
    @Path("stats/{deviceId}/sensors/{sensorName}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieve Sensor data for the device type",
            notes = "",
            response = Response.class,
            tags = "android_sense",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidSenseConstants.SCOPE, value = "perm:android-sense:enroll")
                    })
            }
    )
    Response getAndroidSenseDeviceStats(@PathParam("deviceId") String deviceId, @PathParam("sensorName") String sensor,
                                        @QueryParam("from") long from, @QueryParam("to") long to);

    /**
     * Enroll devices.
     */
    @Path("device/{device_id}/register")
    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Enroll device",
            notes = "",
            response = Response.class,
            tags = "android_sense",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = AndroidSenseConstants.SCOPE, value = "perm:android-sense:enroll")
                    })
            }
    )
    Response register(@PathParam("device_id") String deviceId, @QueryParam("deviceName") String deviceName);

}

