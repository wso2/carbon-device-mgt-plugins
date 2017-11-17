/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.wso2.carbon.iot.android.sense.realtimeviewer.sensorlisting;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to save the list of sensors that are available in the device, which are supported by the iot server.
 * This list will be saved in Shared preferences so that app can use this data when needed.
 */
public class AvailableSensorsInDevice {

    private SharedPreferences sensorPreference;

    /**
     * The Android sensor manager which is used to get the sensors available in device.
     */
    private SensorManager mSensorManager;

    public AvailableSensorsInDevice(Context context) {
        this.sensorPreference = context.getSharedPreferences(SupportedSensors.AVAILABLE_SENSORS, 0);
        this.mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * This method filters the pre defined sensor types from sensors available in device and sets them in Shared
     * preferences.
     */
    public void setContent() {
        SupportedSensors supportedSensors = SupportedSensors.getInstance();
        List<String> sensor_List = supportedSensors.getSensorList();
        Set<String> sensorSet = new HashSet<>();
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        for (String sen : sensor_List) {
            if (sensors.contains(mSensorManager.getDefaultSensor(supportedSensors.getType(sen.toLowerCase())))) {
                sensorSet.add(sen);
            }
        }

        SharedPreferences.Editor editor = this.sensorPreference.edit();
        editor.putStringSet(SupportedSensors.GET_AVAILABLE_SENSORS, sensorSet);
        editor.apply();
    }
}
