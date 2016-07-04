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

package org.wso2.carbon.device.mgt.iot.arduino.service.impl;

import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@API(name = "arduino", version = "1.0.0", context = "/arduino", tags = {"arduino"})
@DeviceType(value = "arduino")
public interface ArduinoService {

    @Path("device/{deviceId}/bulb")
    @POST
    @Feature(code = "bulb", name = "Control Bulb", description = "Control Bulb on Arduino Uno")
    @Permission(scope = "arduino_user", roles = {"emm-user"})
    Response switchBulb(@PathParam("deviceId") String deviceId, @QueryParam("state") String state);

    @Path("device/{deviceId}/controls")
    @GET
    @Permission(scope = "arduino_device", roles = {"emm-user"})
    Response readControls(@PathParam("deviceId") String deviceId);

    /**
     * Retreive Sensor data for the device type
     */
    @Path("device/stats/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Permission(scope = "arduino_user", roles = {"emm-user"})
    Response getArduinoTemperatureStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                                               @QueryParam("to") long to);

    /**
     * download device agent
     */
    @Path("device/download")
    @GET
    @Produces("application/octet-stream")
    @Permission(scope = "arduino_user", roles = {"emm-user"})
    Response downloadSketch(@QueryParam("deviceName") String customDeviceName);

}
