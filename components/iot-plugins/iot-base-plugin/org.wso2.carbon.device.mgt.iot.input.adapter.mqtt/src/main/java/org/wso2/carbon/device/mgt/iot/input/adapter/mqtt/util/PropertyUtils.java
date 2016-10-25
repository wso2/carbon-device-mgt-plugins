/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.device.mgt.iot.input.adapter.mqtt.util;

import org.wso2.carbon.core.util.Utils;

public class PropertyUtils {
    private static final String MQTT_PORT = "\\$\\{mqtt.broker.port\\}";
    private static final String MQTT_BROKER_HOST = "\\$\\{mqtt.broker.host\\}";
    private static final String DEFAULT_CARBON_LOCAL_IP_PROPERTY = "carbon.local.ip";
    private static final int DEFAULT_MQTT_PORT = 1883;

    //This method is only used if the mb features are within DAS.
    public static String replaceMqttProperty (String urlWithPlaceholders) {
        urlWithPlaceholders = Utils.replaceSystemProperty(urlWithPlaceholders);
        String portOffset = System.getProperty("portOffset");
        urlWithPlaceholders = urlWithPlaceholders.replaceAll(MQTT_PORT, ""
                + (DEFAULT_MQTT_PORT + Integer.parseInt(portOffset.trim())));
        urlWithPlaceholders = urlWithPlaceholders.replaceAll(MQTT_BROKER_HOST, System.getProperty(
                DEFAULT_CARBON_LOCAL_IP_PROPERTY, "localhost"));
        return urlWithPlaceholders;
    }
}
