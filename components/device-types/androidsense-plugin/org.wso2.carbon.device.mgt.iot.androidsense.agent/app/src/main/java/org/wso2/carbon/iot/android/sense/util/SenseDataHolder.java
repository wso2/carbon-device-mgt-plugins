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

import org.wso2.carbon.iot.android.sense.beacon.BeaconScanedData;
import org.wso2.carbon.iot.android.sense.event.streams.Location.LocationData;
import org.wso2.carbon.iot.android.sense.event.streams.Sensor.SensorData;
import org.wso2.carbon.iot.android.sense.event.streams.Speed.SpeedData;
import org.wso2.carbon.iot.android.sense.event.streams.activity.ActivityData;
import org.wso2.carbon.iot.android.sense.event.streams.application.ApplicationData;
import org.wso2.carbon.iot.android.sense.event.streams.audio.AudioData;
import org.wso2.carbon.iot.android.sense.event.streams.battery.BatteryData;
import org.wso2.carbon.iot.android.sense.event.streams.call.CallData;
import org.wso2.carbon.iot.android.sense.event.streams.screen.ScreenData;
import org.wso2.carbon.iot.android.sense.event.streams.sms.SmsData;
import org.wso2.carbon.iot.android.sense.speech.detector.util.WordData;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * This holds the sensor,battery and location data inmemory.
 */
public class SenseDataHolder {

    private static List<SensorData> sensorDataHolder;
    private static List<BatteryData> batteryDataHolder;
    private static List<CallData> callDataHolder;
    private static List<LocationData> locationDataHolder;
    private static List<WordData> wordDataHolder;
    private static List<SpeedData> speedDataHolder;
    private static List<BeaconScanedData> beaconScanedDataHolder;
    private static List<ScreenData> screenDataHolder;
    private static List<AudioData> audioDataHolder;
    private static List<ActivityData> activityDataHolder;
    private static List<SmsData> smsDataHolder;
    private static List<ApplicationData> applicationDataHolder;
    //LocationData gps;


    private static final String TAG = SenseDataHolder.class.getName();


    public static List<SensorData> getSensorDataHolder() {
        if (sensorDataHolder == null) {
            sensorDataHolder = new CopyOnWriteArrayList<>();
        }
        return sensorDataHolder;
    }

    public static List<BatteryData> getBatteryDataHolder() {
        if (batteryDataHolder == null) {
            batteryDataHolder = new CopyOnWriteArrayList<>();
        }
        return batteryDataHolder;
    }

    public static List<CallData> getCallDataHolder() {
        if (callDataHolder == null) {
            callDataHolder = new CopyOnWriteArrayList<>();
        }
        return callDataHolder;
    }

    public static List<LocationData> getLocationDataHolder() {


        if (locationDataHolder == null) {

            locationDataHolder = new CopyOnWriteArrayList<>();
        }
        return locationDataHolder;

    }

    public static List<WordData> getWordDataHolder() {
        if (wordDataHolder == null) {
            wordDataHolder = new CopyOnWriteArrayList<>();
        }
        return wordDataHolder;
    }

    public static List<SpeedData> getSpeedDataHolder() {
        if (speedDataHolder == null) {
            speedDataHolder = new CopyOnWriteArrayList<>();
        }
        return speedDataHolder;
    }

    public static List<BeaconScanedData> getBeaconScanedDataHolder() {
        if (beaconScanedDataHolder == null) {
            beaconScanedDataHolder = new CopyOnWriteArrayList<>();
        }
        return beaconScanedDataHolder;
    }

    public static List<ScreenData> getScreenDataHolder() {
        if (screenDataHolder == null) {
            screenDataHolder = new CopyOnWriteArrayList<>();
        }
        return screenDataHolder;
    }

    public static List<AudioData> getAudioDataHolder() {
        if (audioDataHolder == null) {
            audioDataHolder = new CopyOnWriteArrayList<>();
        }
        return audioDataHolder;
    }

    public static List<ActivityData> getActivityDataHolder() {
        if (activityDataHolder == null) {
            activityDataHolder = new CopyOnWriteArrayList<>();
        }
        return activityDataHolder;
    }

    public static List<SmsData> getSmsDataHolder() {
        if (smsDataHolder == null) {
            smsDataHolder = new CopyOnWriteArrayList<>();
        }
        return smsDataHolder;
    }

    public static List<ApplicationData> getApplicationDataHolder() {
        if (applicationDataHolder == null) {
            applicationDataHolder = new CopyOnWriteArrayList<>();
        }
        return applicationDataHolder;
    }

    public static void resetSensorDataHolder() {
        sensorDataHolder = null;
    }

    public static void resetBatteryDataHolder() {
        batteryDataHolder = null;
    }

    public static void resetCallDataHolder() {
        callDataHolder = null;
    }

    public static void resetLocationDataHolder() {
        locationDataHolder = null;
    }

    public static void resetWordDataHolder() {
        wordDataHolder = null;
    }

    public static void resetSpeedDataHolder() {
        speedDataHolder = null;
    }

    public static void resetBeaconScanedDataHolder() {
        beaconScanedDataHolder = null;
    }

    public static void resetScreenDataHolder() {
        screenDataHolder = null;
    }

    public static void resetAudioDataHolder() {
        audioDataHolder = null;
    }

    public static void resetActivityDataHolder() {
        activityDataHolder = null;
    }

    public static void resetSmsDataHolder() {
        smsDataHolder = null;
    }

    public static void resetApplicationDataHolder() {
        applicationDataHolder = null;
    }
}
