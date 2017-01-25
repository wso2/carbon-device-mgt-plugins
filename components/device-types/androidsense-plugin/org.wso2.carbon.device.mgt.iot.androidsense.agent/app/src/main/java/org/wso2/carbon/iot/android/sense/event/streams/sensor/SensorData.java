/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.iot.android.sense.event.streams.sensor;

import android.hardware.SensorEvent;

import org.wso2.carbon.iot.android.sense.realtimeviewer.sensorlisting.SupportedSensors;

import java.util.Date;

/**
 * This defines the data structure of the sensor data that is been collected.
 * look at http://developer.android.com/guide/topics/sensors/sensors_overview.html for field description.
 */
public class SensorData {
    private int sensorType;
    private String sensorName;
    private String sensorVendor;
    private float sensorValues[];
    private int accuracyStatus;
    private long timestamp;
    private String collectTimestamp;
    private SupportedSensors supportedSensors = SupportedSensors.getInstance();

    SensorData(SensorEvent event) {
        sensorValues = event.values;
        accuracyStatus = event.accuracy;
        collectTimestamp = String.valueOf(event.timestamp);
        timestamp = new Date().getTime();
        sensorName = supportedSensors.getType(event.sensor.getType()).toUpperCase();
        sensorVendor = event.sensor.getVendor();
        sensorType = event.sensor.getType();
    }

    public int getSensorType() {
        return sensorType;
    }

    public void setSensorType(int sensorType) {
        this.sensorType = sensorType;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getSensorVendor() {
        return sensorVendor;
    }

    public void setSensorVendor(String sensorVendor) {
        this.sensorVendor = sensorVendor;
    }

    public float[] getSensorValues() {
        return sensorValues;
    }

    public void setSensorValues(float sensorValues[]) {
        this.sensorValues = sensorValues;
    }

    public int getAccuracyStatus() {
        return accuracyStatus;
    }

    public void setAccuracyStatus(int accuracyStatus) {
        this.accuracyStatus = accuracyStatus;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCollectTimestamp() {
        return collectTimestamp;
    }

    public void setCollectTimestamp(String collectTimestamp) {
        this.collectTimestamp = collectTimestamp;
    }
}
