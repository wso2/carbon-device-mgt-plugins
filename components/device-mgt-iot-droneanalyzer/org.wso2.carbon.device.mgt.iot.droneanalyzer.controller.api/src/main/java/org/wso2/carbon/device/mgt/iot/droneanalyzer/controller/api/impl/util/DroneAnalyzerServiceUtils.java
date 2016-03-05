
/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl.util;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.controller.DroneController;

public class DroneAnalyzerServiceUtils {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneAnalyzerServiceUtils.class);

    /**
     * Send controlling command to device
     *
     * @param controller device specific controller implementation
     * @param deviceId   unique identifier for each device
     * @param action     which action to be executed on device e.g.: land, take off, up, down and so on..
     * @param duration   duration which will execute given action e.g.:  up, down and so on..
     * @param speed      at what speed given action is being executed e.g.:  up, down and so on..
     * @return status
     * @throws DeviceManagementException
     */
    public static boolean sendControlCommand(DroneController controller, String deviceId, String action,
                                             double speed, double duration)
            throws DeviceManagementException {
        boolean controlState = false;
        try{
            switch (action){
                case DroneConstants.TAKE_OFF:
                    controlState = controller.takeoff();
                    break;
                case DroneConstants.LAND:
                    controlState = controller.land();
                    break;
                case DroneConstants.BACK:
                    controlState = controller.back(speed, duration);
                    break;
                case DroneConstants.CLOCK_WISE:
                    controlState = controller.clockwise(speed, duration);
                    break;
                case DroneConstants.COUNTER_CLOCKWISE:
                    controlState = controller.conterClockwise(speed, duration);
                    break;
                case DroneConstants.DOWN:
                    controlState = controller.down(speed, duration);
                    break;
                case DroneConstants.FORWARD:
                    controlState = controller.front(speed, duration);
                    break;
                case DroneConstants.UP:
                    controlState = controller.up(speed, duration);
                    break;
                default:
                    log.error("Invalid command");
                    break;
            }
        }catch(Exception e){
            log.error(e.getMessage()+ "\n"+ e);
        }
        return controlState;
    }
}
