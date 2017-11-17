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

import android.hardware.Sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class to store the supported sensorDataMap types.
 */
public class SupportedSensors {

    //For set user selected sensors. Will be used by sensorDataMap reading and dialog
    public static String SELECTED_SENSORS = "Selected";
    public static String SELECTED_SENSORS_BY_USER = "userSelection";

    //For setting the available sensors in the device in dialog and AvailableSensorsInDevice
    public static String AVAILABLE_SENSORS = "Sensors";
    public static String GET_AVAILABLE_SENSORS = "getAvailableSensors";

    public static final int SUPPORTED_SENSOR_COUNT = 10;
    private static List<String> sensorList = new ArrayList<>();
    private static HashMap<String, Integer> sensorTypeMap = new HashMap<>();
    private static HashMap<Integer, String> typeSensorMap = new HashMap<>();
    private static SupportedSensors supportedSensors = new SupportedSensors();

    private SupportedSensors() {
        this.setList();
        this.setSensorTypeMap();
        this.setTypeSensorMap();
    }

    public static SupportedSensors getInstance() {
        return supportedSensors;
    }

    /**
     * Set the supported sensor types by the IOT server.
     */
    private void setList() {
        sensorList.add("Accelerometer");
        sensorList.add("Magnetometer");
        sensorList.add("Gravity");
        sensorList.add("Rotation Vector");
        sensorList.add("Pressure");
        sensorList.add("Light");
        sensorList.add("Gyroscope");
        sensorList.add("Proximity");
        sensorList.add("Pedometer");
    }

    /**
     * Populate the hash map which has Sensor name as the key and the sensor type as the value.
     */
    private void setSensorTypeMap() {
        sensorTypeMap.put("accelerometer", Sensor.TYPE_ACCELEROMETER);
        sensorTypeMap.put("magnetometer", Sensor.TYPE_MAGNETIC_FIELD);
        sensorTypeMap.put("gravity", Sensor.TYPE_GRAVITY);
        sensorTypeMap.put("rotation vector", Sensor.TYPE_GAME_ROTATION_VECTOR);
        sensorTypeMap.put("pressure", Sensor.TYPE_PRESSURE);
        sensorTypeMap.put("gyroscope", Sensor.TYPE_GYROSCOPE);
        sensorTypeMap.put("light", Sensor.TYPE_LIGHT);
        sensorTypeMap.put("proximity", Sensor.TYPE_PROXIMITY);
        sensorTypeMap.put("pedometer", Sensor.TYPE_STEP_COUNTER);
    }

    /**
     * Populates the hash map which has Sensor type as the key and sensor name as the value.
     */
    private void setTypeSensorMap() {
        typeSensorMap.put(Sensor.TYPE_ACCELEROMETER, "accelerometer");
        typeSensorMap.put(Sensor.TYPE_MAGNETIC_FIELD, "magnetometer");
        typeSensorMap.put(Sensor.TYPE_GRAVITY, "gravity");
        typeSensorMap.put(Sensor.TYPE_GAME_ROTATION_VECTOR, "rotation vector");
        typeSensorMap.put(Sensor.TYPE_PRESSURE, "pressure");
        typeSensorMap.put(Sensor.TYPE_GYROSCOPE, "gyroscope");
        typeSensorMap.put(Sensor.TYPE_LIGHT, "light");
        typeSensorMap.put(Sensor.TYPE_PROXIMITY, "proximity");
        typeSensorMap.put(Sensor.TYPE_STEP_COUNTER, "pedometer");
    }

    /**
     * Method to get the supported sensor list.
     *
     * @return the list of sensors supported by the iot server.
     */
    public List<String> getSensorList() {
        return sensorList;
    }


    /**
     * @param sensor The name of the sensor.
     * @return The integer representing the type of the sensor,
     */
    public int getType(String sensor) {
        return sensorTypeMap.get(sensor);
    }


    /**
     * @param type The type of the sensor.
     * @return The sensor name related to the given sensor type.
     */
    public String getType(int type) {
        return typeSensorMap.get(type);
    }
}
