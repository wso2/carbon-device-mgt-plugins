
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
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.controller.DroneController;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl.exception.DroneAnalyzerException;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl.transport.DroneAnalyzerXMPPConnector;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;

import java.io.File;

public class DroneAnalyzerServiceUtils {

    private static final String SUPER_TENANT = "carbon.super";
    private static final String TEMPERATURE_STREAM_DEFINITION = "org.wso2.iot.devices.temperature";
    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneAnalyzerServiceUtils.class);

    public static void sendCommandViaXMPP(String deviceOwner, String deviceId, String resource,
                                          String state, DroneAnalyzerXMPPConnector droneXMPPConnector)
            throws DeviceManagementException, TransportHandlerException {

        String xmppServerDomain = XmppConfig.getInstance().getXmppEndpoint();
        int indexOfChar = xmppServerDomain.lastIndexOf(File.separator);
        if (indexOfChar != -1) {
            xmppServerDomain = xmppServerDomain.substring((indexOfChar + 1),
                    xmppServerDomain.length());
        }
        indexOfChar = xmppServerDomain.indexOf(":");
        if (indexOfChar != -1) {
            xmppServerDomain = xmppServerDomain.substring(0, indexOfChar);
        }
        String clientToConnect = deviceId + "@" + xmppServerDomain + File.separator + deviceOwner;
        String message = resource.replace("/", "") + ":" + state;
        droneXMPPConnector.publishDeviceData(clientToConnect, message, "CONTROL-REQUEST");
    }

    public static boolean sendControlCommand(DroneController controller, String deviceId, String action, double speed, double duration)
            throws DeviceManagementException {
        boolean control_state = false;
        try{
            switch (action){
                case DroneConstants.TAKE_OFF:
                    control_state = controller.takeoff();
                    break;
                case DroneConstants.LAND:
                    control_state = controller.land();
                    break;
                case DroneConstants.BACK:
                    control_state = controller.back(speed, duration);
                    break;
                case DroneConstants.CLOCK_WISE:
                    control_state = controller.clockwise(speed, duration);
                    break;
                case DroneConstants.COUNTER_CLOCKWISE:
                    control_state = controller.conterClockwise(speed, duration);
                    break;
                case DroneConstants.DOWN:
                    control_state = controller.down(speed, duration);
                    break;
                case DroneConstants.FRONT:
                    control_state = controller.back(speed, duration);
                    break;
                case DroneConstants.FORWARD:
                    control_state = controller.clockwise(speed, duration);
                    break;
                case DroneConstants.UP:
                    control_state = controller.up(speed, duration);
                    break;
            }
        }catch(Exception e){
            log.error(e.getMessage()+ "\n"+ e);
        }
        return control_state;
    }

    public static boolean publishToDAS(String owner, String deviceId, float temperature) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain(SUPER_TENANT, true);
        DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx.getOSGiService(
                DeviceAnalyticsService.class, null);
        Object metdaData[] = {owner, DroneConstants.DEVICE_TYPE, deviceId,
                System.currentTimeMillis()};
        Object payloadData[] = {temperature};

        try {
            deviceAnalyticsService.publishEvent(TEMPERATURE_STREAM_DEFINITION, "1.0.0", metdaData,
                    new Object[0], payloadData);
        } catch (DataPublisherConfigurationException e) {
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return true;
    }

    public static CertificateManagementService getCertificateManagementService() throws
            DroneAnalyzerException {

        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        CertificateManagementService certificateManagementService = (CertificateManagementService)
                ctx.getOSGiService(CertificateManagementService.class, null);

        if (certificateManagementService == null) {
            String msg = "EnrollmentService is not initialized";
            log.error(msg);
            throw new DroneAnalyzerException(msg);
        }

        return certificateManagementService;
    }

}
