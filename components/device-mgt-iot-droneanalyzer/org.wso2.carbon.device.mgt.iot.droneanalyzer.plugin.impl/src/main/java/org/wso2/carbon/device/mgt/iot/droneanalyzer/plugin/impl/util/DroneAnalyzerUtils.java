package org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Created by geesara on 12/9/15.
 */
public class DroneAnalyzerUtils {
    private static Log log = LogFactory.getLog(DroneAnalyzerUtils.class);

    public static String getDeviceProperty(Map<String, String> deviceProperties, String property) {
        String deviceProperty = deviceProperties.get(property);
        if (deviceProperty == null) {
            return "";
        }
        return deviceProperty;
    }
}
