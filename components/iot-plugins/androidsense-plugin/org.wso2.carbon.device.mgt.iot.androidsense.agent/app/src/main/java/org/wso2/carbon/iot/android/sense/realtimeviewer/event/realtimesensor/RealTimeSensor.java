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
package org.wso2.carbon.iot.android.sense.realtimeviewer.event.realtimesensor;

import android.support.annotation.NonNull;

/**
 * The class to store the sensor data captured by the RealTimeSensorReader.
 */
public class RealTimeSensor implements Comparable {


    /**
     * Name of the sensor.
     */
    private String name;

    /**
     * The X value reading of the sensor.
     */
    private String valueX;

    /**
     * The Y value reading of the sensor.
     */
    private String valueY;

    /**
     * The Y value reading of the sensor.
     */
    private String valueZ;

    public RealTimeSensor() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueX() {
        return valueX;
    }

    public void setValueX(String valueX) {
        this.valueX = valueX;
    }

    public String getValueY() {
        return valueY;
    }

    public void setValueY(String valueY) {
        this.valueY = valueY;
    }

    public String getValueZ() {
        return valueZ;
    }

    public void setValueZ(String valueZ) {
        this.valueZ = valueZ;
    }

    @Override
    public String toString() {
        return this.valueX + ", " + valueY + ", " + valueZ;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return this.toString().contains(another.toString()) ? 1 : 0;
    }
}
