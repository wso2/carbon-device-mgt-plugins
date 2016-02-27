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
}
