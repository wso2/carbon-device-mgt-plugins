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

package org.wso2.carbon.device.mgt.iot.raspberrypi.service.impl;

import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;
import org.wso2.carbon.device.mgt.iot.raspberrypi.service.impl.dto.DeviceData;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@API(name = "raspberrypi", version = "1.0.0", context = "/raspberrypi", tags = {"raspberrypi"})
@DeviceType(value = "raspberrypi")
public interface RaspberryPiControllerService {

    @Path("device/register/{deviceId}/{ip}/{port}")
    @POST
    Response registerDeviceIP(@PathParam("deviceId") String deviceId, @PathParam("ip") String deviceIP,
                              @PathParam("port") String devicePort, @Context HttpServletRequest request);

    @Path("device/{deviceId}/bulb")
    @POST
    @Feature(code = "bulb", name = "Bulb On / Off", type = "operation",
            description = "Switch on/off Raspberry Pi agent's bulb. (On / Off)")
    Response switchBulb(@PathParam("deviceId") String deviceId, @FormParam("state") String state);

    @Path("device/{deviceId}/readtemperature")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "readtemperature", name = "Temperature", type = "monitor",
            description = "Request temperature reading from Raspberry Pi agent")
    Response requestTemperature(@PathParam("deviceId") String deviceId);

    @Path("device/push_temperature")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response pushTemperatureData(final DeviceData dataMsg, @Context HttpServletRequest request);

    /**
     * Retreive Sensor data for the device type
     */
    @Path("device/stats/{deviceId}/sensors/temperature")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    Response getArduinoTemperatureStats(@PathParam("deviceId") String deviceId, @QueryParam("username") String user,
                                        @QueryParam("from") long from, @QueryParam("to") long to);

}
