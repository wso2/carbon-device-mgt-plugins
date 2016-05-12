/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.iot.androidsense.plugin.constants;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

public class AndroidSenseConstants {

    public final static String DEVICE_TYPE = "android_sense";
    public final static String DEVICE_PLUGIN_DEVICE_NAME = "DEVICE_NAME";
    public final static String DEVICE_PLUGIN_DEVICE_ID = "ANDROID_DEVICE_ID";
    //Android Sense Stream definitions.
    public static final String ACCELEROMETER_STREAM_DEFINITION = "org.wso2.iot.devices.accelerometer";
    public static final String BATTERY_STREAM_DEFINITION = "org.wso2.iot.devices.battery";
    public static final String GPS_STREAM_DEFINITION = "org.wso2.iot.devices.gps";
    public static final String GRAVITY_STREAM_DEFINITION = "org.wso2.iot.devices.gravity";
    public static final String GYROSCOPE_STREAM_DEFINITION = "org.wso2.iot.devices.gyroscope";
    public static final String LIGHT_STREAM_DEFINITION = "org.wso2.iot.devices.light";
    public static final String MAGNETIC_STREAM_DEFINITION = "org.wso2.iot.devices.magnetic";
    public static final String PRESSURE_STREAM_DEFINITION = "org.wso2.iot.devices.pressure";
    public static final String PROXIMITY_STREAM_DEFINITION = "org.wso2.iot.devices.proximity";
    public static final String ROTATION_STREAM_DEFINITION = "org.wso2.iot.devices.rotation";
    public static final String WORD_COUNT_STREAM_DEFINITION = "org.wso2.iot.devices.wordcount";

    //Android Sensor names
    public static final String SENSOR_ACCELEROMETER = "accelerometer";
    public static final String SENSOR_BATTERY = "battery";
    public static final String SENSOR_GPS = "gps";
    public static final String SENSOR_GRAVITY = "gravity";
    public static final String SENSOR_GYROSCOPE = "gyroscope";
    public static final String SENSOR_LIGHT = "light";
    public static final String SENSOR_MAGNETIC = "magnetic";
    public static final String SENSOR_PRESSURE = "pressure";
    public static final String SENSOR_PROXIMITY = "proximity";
    public static final String SENSOR_ROTATION = "rotation";
    public static final String SENSOR_WORDCOUNT = "wordcounter";
    //MQTT Subscribe topic
    public static final String MQTT_SUBSCRIBE_WORDS_TOPIC = "wso2/+/android_sense/+/data";
    public static final String DATA_SOURCE_NAME = "jdbc/AndroidSenseDM_DB";
    public final static String DEVICE_TYPE_PROVIDER_DOMAIN = "carbon.super";

    //mqtt tranport related constants
    public static final String MQTT_ADAPTER_NAME = "android_sense_mqtt";
    public static final String MQTT_ADAPTER_TYPE = "oauth-mqtt";
    public static final String ADAPTER_TOPIC_PROPERTY = "topic";
    public static final String MQTT_PORT = "\\$\\{mqtt.broker.port\\}";
    public static final String MQTT_BROKER_HOST = "\\$\\{mqtt.broker.host\\}";
    public static final String CARBON_CONFIG_PORT_OFFSET = "Ports.Offset";
    public static final String DEFAULT_CARBON_LOCAL_IP_PROPERTY = "carbon.local.ip";
    public static final int CARBON_DEFAULT_PORT_OFFSET = 0;
    public static final int DEFAULT_MQTT_PORT = 1883;

    public static final String USERNAME_PROPERTY_KEY = "username";
    public static final String DCR_PROPERTY_KEY = "dcrUrl";
    public static final String BROKER_URL_PROPERTY_KEY = "url";
    public static final String SCOPES_PROPERTY_KEY = "scopes";
    public static final String QOS_PROPERTY_KEY = "qos";
    public static final String CLIENT_ID_PROPERTY_KEY = "qos";
    public static final String CLEAR_SESSION_PROPERTY_KEY = "clearSession";
    public static final String TOPIC = "topic";

    public static final String MQTT_CONFIG_LOCATION = CarbonUtils.getEtcCarbonConfigDirPath() + File.separator
            + "mqtt.properties";
}
