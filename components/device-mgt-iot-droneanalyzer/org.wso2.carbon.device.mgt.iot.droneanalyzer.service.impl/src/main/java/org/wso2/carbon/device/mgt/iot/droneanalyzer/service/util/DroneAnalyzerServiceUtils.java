package org.wso2.carbon.device.mgt.iot.droneanalyzer.service.util;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.service.transport.DroneAnalyzerXMPPConnector;

import java.io.File;

/**
 * Created by geesara on 12/9/15.
 */
public class DroneAnalyzerServiceUtils {

    private static final String SUPER_TENANT = "carbon.super";
    private static final String TEMPERATURE_STREAM_DEFINITION = "org.wso2.iot.devices.temperature";

    public static void sendCommandViaXMPP(String deviceOwner, String deviceId, String resource,
                                          String state, DroneAnalyzerXMPPConnector droneXMPPConnector)
            throws DeviceManagementException {

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

        droneXMPPConnector.sendXMPPMessage(clientToConnect, message, "CONTROL-REQUEST");
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
}
