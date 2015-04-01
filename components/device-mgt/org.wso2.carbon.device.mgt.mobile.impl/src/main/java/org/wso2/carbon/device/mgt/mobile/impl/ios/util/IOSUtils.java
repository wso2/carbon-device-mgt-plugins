package org.wso2.carbon.device.mgt.mobile.impl.ios.util;

import java.util.Map;

public class IOSUtils {

    public static String getDeviceProperty(Map<String, String> deviceProperties, String property) {

        String deviceProperty = deviceProperties.get(property);

        if (deviceProperty == null) {
            return "";
        }

        return deviceProperty;
    }
}
