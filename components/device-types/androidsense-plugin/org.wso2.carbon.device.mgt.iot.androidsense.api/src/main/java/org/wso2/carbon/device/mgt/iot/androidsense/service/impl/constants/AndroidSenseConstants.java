/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"
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

package org.wso2.carbon.device.mgt.iot.androidsense.service.impl.constants;

public class AndroidSenseConstants {

    public final static String DEVICE_TYPE = "android_sense";

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

    //mqtt tranport related constants
    public static final String MQTT_ADAPTER_TOPIC_PROPERTY_NAME = "mqtt.adapter.topic";

    public static final String HOST_NAME = "HostName";
    public static final String LOCALHOST = "localhost";
    public static final String CONFIG_TYPE = "general";
    public static final String DEFAULT_ENDPOINT = "tcp://${mqtt.broker.host}:${mqtt.broker.port}";

    public static final String SCOPE = "scope";

    public static final String PERM_ENROLL_ANDROID_SENSE = "/permission/admin/device-mgt/devices/enroll/android-sense";
    public static final String PERM_OWNING_DEVICE_VIEW = "/permission/admin/device-mgt/devices/owning-device/view";

    public static final String ROLE_NAME = "internal/devicemgt-user";

}
