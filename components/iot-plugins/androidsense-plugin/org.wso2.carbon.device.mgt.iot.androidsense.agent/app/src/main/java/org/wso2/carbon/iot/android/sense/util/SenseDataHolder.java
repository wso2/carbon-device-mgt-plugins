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
package org.wso2.carbon.iot.android.sense.util;

import org.wso2.carbon.iot.android.sense.event.streams.Location.LocationData;
import org.wso2.carbon.iot.android.sense.event.streams.Sensor.SensorData;
import org.wso2.carbon.iot.android.sense.event.streams.battery.BatteryData;
import org.wso2.carbon.iot.android.sense.speech.detector.util.WordData;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import android.util.Log;


/**
 * This holds the sensor,battery and location data inmemory.
 */
public class SenseDataHolder {

    private static List<SensorData> sensorDataHolder;
    private static List<BatteryData> batteryDataHolder;
    private static List<LocationData> locationDataHolder;
    private static List<WordData> wordDataHolder;

    //LocationData gps;


    private static final String TAG = SenseDataHolder.class.getName();


    public static List<SensorData> getSensorDataHolder(){
        if(sensorDataHolder == null){
            sensorDataHolder = new CopyOnWriteArrayList<>();
        }
        return sensorDataHolder;
    }

    public static List<BatteryData> getBatteryDataHolder(){
        if(batteryDataHolder == null){
            batteryDataHolder = new CopyOnWriteArrayList<>();
        }
        return batteryDataHolder;
    }

    public static List<LocationData> getLocationDataHolder(){


        if(locationDataHolder == null){

            locationDataHolder = new CopyOnWriteArrayList<>();
        }
            return locationDataHolder;

    }

    public static List<WordData> getWordDataHolder(){
        if(wordDataHolder == null){
            wordDataHolder = new CopyOnWriteArrayList<>();
        }
        return wordDataHolder;
    }

    public static void resetSensorDataHolder(){
        sensorDataHolder = null;
    }

    public static void resetBatteryDataHolder(){
        batteryDataHolder = null;
    }

    public static void resetLocationDataHolder(){
        locationDataHolder = null;
    }

    public static void resetWordDataHolder() {
        wordDataHolder = null;
    }

}
