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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl;

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class consists the functions/APIs specific to the "actions" of the VirtualFirealarm device-type. These APIs
 * include the ones that are used by the [Device] to contact the server (i.e: Enrollment & Publishing Data) and the
 * ones used by the [Server/Owner] to contact the [Device] (i.e: sending control signals). This class also initializes
 * the transport 'Connectors' [XMPP & MQTT] specific to the VirtualFirealarm device-type in order to communicate with
 * such devices and to receive messages form it.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "virtual_firealarm"),
                                @ExtensionProperty(name = "context", value = "/virtual_firealarm"),
                        })
                }
        ),
        tags = {
                @Tag(name = "virtual_firealarm", description = "")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Enroll device",
                        description = "",
                        key = "perm:firealarm:enroll",
                        permissions = {"/device-mgt/devices/enroll/firealarm"}
                )
        }
)
public interface VirtualFireAlarmService {

    String SCOPE = "scope";

    /**
     * This is an API called/used from within the Server(Front-End) or by a device Owner. It sends a control command to
     * the VirtualFirealarm device to switch `ON` or `OFF` its buzzer. The method also takes in the protocol to be used
     * to connect-to and send the command to the device.
     *
     * @param deviceId the ID of the VirtualFirealarm device on which the buzzer needs to switched `ON` or `OFF`.
     * @param state    the state to which the buzzer on the device needs to be changed. Either "ON" or "OFF".
     *                 (Case-Insensitive String)
     */
    @POST
    @Path("device/{deviceId}/buzz")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Switch Buzzer",
            notes = "",
            response = Response.class,
            tags = "virtual_firealarm",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:firealarm:enroll")
                    })
            }
    )
    Response switchBuzzer(@PathParam("deviceId") String deviceId,
                             @FormParam("state") String state);

    /**
     * Retrieve Sensor data for the device type
     */
    @Path("device/stats/{deviceId}")
    @GET
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieve Sensor data for the device type",
            notes = "",
            response = Response.class,
            tags = "virtual_firealarm",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:firealarm:enroll")
                    })
            }
    )
    @Consumes("application/json")
    @Produces("application/json")
    Response getVirtualFirealarmStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                                                 @QueryParam("to") long to);

    @Path("device/download")
    @GET
    @Produces("application/zip")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Download agent",
            notes = "",
            response = Response.class,
            tags = "virtual_firealarm",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:firealarm:enroll")
                    })
            }
    )
    Response downloadSketch(@QueryParam("deviceName") String deviceName, @QueryParam("sketchType") String sketchType);

}
