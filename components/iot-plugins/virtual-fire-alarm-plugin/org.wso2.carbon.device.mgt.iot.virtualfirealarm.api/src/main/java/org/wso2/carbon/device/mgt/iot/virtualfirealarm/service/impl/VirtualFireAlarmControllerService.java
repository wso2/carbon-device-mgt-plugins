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

import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.dto.DeviceData;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This class consists the functions/APIs specific to the "actions" of the VirtualFirealarm device-type. These APIs
 * include the ones that are used by the [Device] to contact the server (i.e: Enrollment & Publishing Data) and the
 * ones used by the [Server/Owner] to contact the [Device] (i.e: sending control signals). This class also initializes
 * the transport 'Connectors' [XMPP & MQTT] specific to the VirtualFirealarm device-type in order to communicate with
 * such devices and to receive messages form it.
 */
@API(name = "virtual_firealarm", version = "1.0.0", context = "/virtual_firealarm", tags = {"virtual_firealarm"})
@DeviceType(value = "virtual_firealarm")
public interface VirtualFireAlarmControllerService {

    /**
     * This is an API used/called by the device. It registers the IP of a VirtualFirealarm device against its DeviceID
     * when the device connects with the server for the first time. This DeviceID to IP mapping is necessary only for
     * cases where HTTP communication is to be used. At such instances, this mapping is used by the server to
     * identify the IP of the device to which it has some message to be sent. This method becomes useful only in
     * scenarios where HTTP communication is used in a setup where the IoT-Server and the devices communicating with it
     * are in the same IP network.
     *
     * @param deviceId   the ID of the VirtualFirealarm device from which this register-IP call was initiated.
     * @param deviceIP   the IP of the VirtualFirealarm device which has sent this register-IP request.
     * @param devicePort the PORT on the VirtualFirealarm device (on this IP) that's open for HTTP communication.
     * @param request    the HTTP servlet request object received by default as part of the HTTP call to this API.
     * @return a custom message indicating whether the DeviceID to IP mapping was successful.
     */
    @POST
    @Path("device/register/{deviceId}/{ip}/{port}")
    @Permission(scope = "virtual_firealarm_admin", permissions = {"device-mgt/virtual_firealarm/admin"})
    Response registerDeviceIP(@PathParam("deviceId") String deviceId, @PathParam("ip") String deviceIP,
                                   @PathParam("port") String devicePort, @Context HttpServletRequest request);


    /**
     * This is an API called/used from within the Server(Front-End) or by a device Owner. It sends a control command to
     * the VirtualFirealarm device to switch `ON` or `OFF` its buzzer. The method also takes in the protocol to be used
     * to connect-to and send the command to the device.
     *
     * @param deviceId the ID of the VirtualFirealarm device on which the buzzer needs to switched `ON` or `OFF`.
     * @param protocol the protocol (HTTP, MQTT, XMPP) to be used to connect-to & send the message to the device.
     * @param state    the state to which the buzzer on the device needs to be changed. Either "ON" or "OFF".
     *                 (Case-Insensitive String)
     */
    @POST
    @Path("device/{deviceId}/buzz")
    @Permission(scope = "virtual_firealarm_user", permissions = {"device-mgt/virtual_firealarm/user"})
    @Feature(code = "buzz", name = "Buzzer On / Off", type = "operation",
            description = "Switch on/off Virtual Fire Alarm Buzzer. (On / Off)")
    Response switchBuzzer(@PathParam("deviceId") String deviceId, @QueryParam("protocol") String protocol,
                             @FormParam("state") String state);

    /**
     * This is an API called/used by the VirtualFirealarm device to publish its temperature to the IoT-Server. The
     * received data from the device is stored in a 'DeviceRecord' under the device's ID in the 'SensorDataManager'
     * of the Server.
     *
     * @param dataMsg the temperature data received from the device in JSON format complying to type 'DeviceData'.
     */
    @POST
    @Path("device/temperature")
    @Permission(scope = "virtual_firealarm_admin", permissions = {"device-mgt/virtual_firealarm/admin"})
    @Consumes(MediaType.APPLICATION_JSON)
    Response pushTemperatureData(final DeviceData dataMsg);
    

    /**
     * Retrieve Sensor data for the device type
     */
    @Path("device/stats/{deviceId}/sensors/{sensorName}")
    @GET
    @Permission(scope = "virtual_firealarm_user", permissions = {"device-mgt/virtual_firealarm/user"})
    @Consumes("application/json")
    @Produces("application/json")
    Response getVirtualFirealarmStats(@PathParam("deviceId") String deviceId,
                                                 @PathParam("sensorName") String sensor, @QueryParam("from") long from,
                                                 @QueryParam("to") long to);

}
