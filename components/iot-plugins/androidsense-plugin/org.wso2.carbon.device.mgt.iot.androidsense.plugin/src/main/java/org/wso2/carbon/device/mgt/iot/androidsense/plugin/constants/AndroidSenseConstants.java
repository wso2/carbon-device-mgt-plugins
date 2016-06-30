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
    public final static String DEVICE_TYPE_PROVIDER_DOMAIN = "carbon.super";

    //mqtt tranport related constants
    public static final String MQTT_ADAPTER_TOPIC_PROPERTY_NAME = "mqtt.adapter.topic";

}
