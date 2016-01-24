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
package org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.DeviceValidator;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl.util.DroneAnalyzerServiceUtils;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.controller.DroneController;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.controller.impl.DroneControllerImpl;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.concurrent.ConcurrentHashMap;

@API( name="drone_analyzer", version="1.0.0", context="/drone_analyzer")
@DeviceType( value = "drone_analyzer")
public class DroneControllerService {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneControllerService.class);
    private static final String SUPER_TENANT = "carbon.super";
    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();
    private DroneController droneController = new DroneControllerImpl();

    /*	---------------------------------------------------------------------------------------
                    Device specific APIs - Control APIs + Data-Publishing APIs
            Also contains utility methods required for the execution of these APIs
         ---------------------------------------------------------------------------------------	*/
    @Path("controller/register/{owner}/{deviceId}/{ip}/{port}")
    @POST
    public Response registerDeviceIP(@PathParam("owner") String owner, @PathParam("deviceId") String deviceId,
                                   @PathParam("ip") String deviceIP,
                                   @PathParam("port") String devicePort,
                                   @Context HttpServletResponse response) {
        String result;
        log.info("Got register call from IP: " + deviceIP + " for Device ID: " + deviceId + " of owner: " + owner);
        String deviceHttpEndpoint = deviceIP + ":" + devicePort;
        deviceToIpMap.put(deviceId, deviceHttpEndpoint);
        result = "Device-IP Registered";
        response.setStatus(Response.Status.OK.getStatusCode());
        if (log.isDebugEnabled()) {
            log.debug(result);
        }
        log.info(owner + deviceId + deviceIP + devicePort );
        return Response.ok(Response.Status.OK.getStatusCode()).build();
    }

    @Path("controller/send_command")
    @POST
    /*@Feature( code="send_command", name="Send Command", type="operation",
            description="Send Commands to Drone")*/
    public Response droneController(@HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
                                    @FormParam("action") String action, @FormParam("duration") String duration,
                                    @FormParam("speed") String speed){
        try {
            DeviceValidator deviceValidator = new DeviceValidator();
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(deviceId,
                    DroneConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
        } catch (DeviceManagementException e) {
            log.error("DeviceValidation Failed for deviceId: " + deviceId + " of user: " + owner);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        try {
            DroneAnalyzerServiceUtils.sendControlCommand(droneController, deviceId, action, Double.valueOf(speed),
                    Double.valueOf(duration));
            return Response.status(Response.Status.ACCEPTED).build();

        } catch (DeviceManagementException e) {
           log.error("Drone command didn't success. Try again, \n"+ e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
