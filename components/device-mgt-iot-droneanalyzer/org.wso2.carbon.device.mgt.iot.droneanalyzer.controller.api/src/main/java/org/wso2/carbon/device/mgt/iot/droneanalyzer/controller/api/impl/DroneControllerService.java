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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.DeviceValidator;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl.util.DroneAnalyzerServiceUtils;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.controller.DroneController;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.controller.impl.DroneControllerImpl;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@API( name="drone_analyzer", version="1.0.0", context="/drone_analyzer")
@DeviceType( value = "drone_analyzer")
public class DroneControllerService {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneControllerService.class);
    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;
    private DroneController droneController = new DroneControllerImpl();

    /*	---------------------------------------------------------------------------------------
                    Device specific APIs - Control APIs + Data-Publishing APIs
            Also contains utility methods required for the execution of these APIs
         ---------------------------------------------------------------------------------------	*/

    /**
     * Send controlling command to device
     *
     * @param owner      owner of the device
     * @param deviceId   a unique identifier of device
     * @param action     which action to be executed on device e.g.: land, take off, up, down and so on..
     * @param duration   duration which will execute given action e.g.:  up, down and so on..
     * @param speed      at what speed given action is being executed e.g.:  up, down and so on..
     * @return status
     */
    @Path("controller/send_command")
    @POST
    public Response droneController(@HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
                                    @FormParam("action") String action, @FormParam("duration") String duration,
                                    @FormParam("speed") String speed){
        if(isPermitted(owner, deviceId, response)){
            try {
                DroneAnalyzerServiceUtils.sendControlCommand(droneController, deviceId, action, Double.valueOf(speed),
                        Double.valueOf(duration));
                return Response.status(Response.Status.ACCEPTED).build();
            } catch (DeviceManagementException e) {
                log.error("Drone command didn't success. Try again, \n"+ e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    /**
     * Check user is authorized to perform requested action  or not
     *
     * @param owner     owner of the device
     * @param deviceId  a unique identifier of device
     * @return status
     */
    private boolean isPermitted(String owner, String deviceId, HttpServletResponse response) {
        DeviceValidator deviceValidator = new DeviceValidator();
        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (!deviceValidator.isExist(owner, tenantDomain, new DeviceIdentifier(
                    deviceId, DroneConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            } else {
                return true;
            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return false;
    }
}
