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
package org.wso2.carbon.iot.android.sense.data.publisher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.iot.android.sense.data.publisher.mqtt.AndroidSenseMQTTHandler;
import org.wso2.carbon.iot.android.sense.data.publisher.mqtt.transport.MQTTTransportHandler;
import org.wso2.carbon.iot.android.sense.data.publisher.mqtt.transport.TransportHandlerException;
import org.wso2.carbon.iot.android.sense.constants.SenseConstants;
import org.wso2.carbon.iot.android.sense.event.streams.Location.LocationData;
import org.wso2.carbon.iot.android.sense.event.streams.Sensor.SensorData;
import org.wso2.carbon.iot.android.sense.event.streams.Speed.SpeedData;
import org.wso2.carbon.iot.android.sense.event.streams.Speed.SpeedDataReader;

import org.wso2.carbon.iot.android.sense.event.streams.battery.BatteryData;
import org.wso2.carbon.iot.android.sense.speech.detector.util.ProcessWords;
import org.wso2.carbon.iot.android.sense.speech.detector.util.WordData;
import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;
import org.wso2.carbon.iot.android.sense.util.LocalRegistry;
//import org.wso2.carbon.iot.android.sense.util.SenseClient;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an android service which publishes the data to the server.
 */
public class DataPublisherService extends Service {
    private static final String TAG = DataPublisherService.class.getName();
    private static String KEY_TAG = "key";
    private static String TIME_TAG = "time";
    private static String VALUE_TAG = "value";
    public static Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        Log.d(TAG, "service started");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    List<Event> events = new ArrayList<>();
                    //retrieve sensor data.
                    List<SensorData> sensorDataMap = SenseDataHolder.getSensorDataHolder();
                    if (!sensorDataMap.isEmpty()) {
                        for (SensorData sensorData : sensorDataMap) {
                            Event event = new Event();
                            event.setTimestamp(sensorData.getTimestamp());
                            switch (sensorData.getSensorType()) {
                                case Sensor.TYPE_ACCELEROMETER:
                                    event.setAccelerometer(sensorData.getSensorValues());
                                    events.add(event);
                                    break;
                                case Sensor.TYPE_MAGNETIC_FIELD:
                                    event.setMagnetic(sensorData.getSensorValues());
                                    events.add(event);
                                    break;
                                case Sensor.TYPE_GYROSCOPE:
                                    event.setGyroscope(sensorData.getSensorValues());
                                    events.add(event);
                                    break;
                                case Sensor.TYPE_LIGHT:
                                    event.setLight(sensorData.getSensorValues()[0]);
                                    break;
                                case Sensor.TYPE_PRESSURE:
                                    event.setPressure(sensorData.getSensorValues()[0]);
                                    events.add(event);
                                    break;
                                case Sensor.TYPE_PROXIMITY:
                                    event.setProximity(sensorData.getSensorValues()[0]);
                                    events.add(event);
                                    break;
                                case Sensor.TYPE_GRAVITY:
                                    event.setGravity(sensorData.getSensorValues());
                                    events.add(event);
                                    break;
                                case Sensor.TYPE_GAME_ROTATION_VECTOR:
                                    event.setRotation(sensorData.getSensorValues());
                                    events.add(event);
                                    break;
                            }
                        }
                    }
                    SenseDataHolder.resetSensorDataHolder();

                    //retrieve batter data.
                    List<BatteryData> batteryDataMap = SenseDataHolder.getBatteryDataHolder();
                    if (!batteryDataMap.isEmpty()) {
                        for (BatteryData batteryData : batteryDataMap) {
                            Event event = new Event();
                            event.setTimestamp(batteryData.getTimestamp());
                            event.setBattery(batteryData.getLevel());
                            events.add(event);
                        }
                    }
                    SenseDataHolder.resetBatteryDataHolder();

                    //retrieve speed data.
                    List<SpeedData> speedDataMap = SenseDataHolder.getSpeedDataHolder();
                    if (!speedDataMap.isEmpty()) {
                        for (SpeedData speedData : speedDataMap) {
                            Event event = new Event();
                            event.setTimestamp(speedData.getTimeStamp());
                            event.setSpeed(speedData.getSpeed());
                            event.setTurns(speedData.getTurns());
                            events.add(event);
                        }
                    }
                    SenseDataHolder.resetSpeedDataHolder();

                    //retrieve location data.
                    List<LocationData> locationDataMap = SenseDataHolder.getLocationDataHolder();

                    if (!locationDataMap.isEmpty()) {
                        for (LocationData locationData : locationDataMap) {
                            Event event = new Event();
                            event.setTimestamp(locationData.getTimeStamp());
                            event.setGps(new double[]{locationData.getLatitude(), locationData.getLongitude()});
                            events.add(event);
                        }
                     }
                    SenseDataHolder.resetLocationDataHolder();

                    //retrieve words
                    ProcessWords.cleanAndPushToWordMap();
                    List<WordData> wordDatMap = SenseDataHolder.getWordDataHolder();
                    for (WordData wordData : wordDatMap) {
                        if (wordData.getOccurences() == 0) {
                            continue;
                        }
                        for (int i = 0; i < wordData.getOccurences(); i++) {
                            Event event = new Event();
                            event.setTimestamp(wordData.getTimestamp());
                            event.setWord(wordData.getWord());
                            String word = wordData.getWord();
                            String status = word;
                            if ((!word.equals(SenseConstants.EVENT_LISTENER_STARTED)) && (!word.equals(SenseConstants
                                    .EVENT_LISTENER_FINISHED))) {
                                status = SenseConstants.EVENT_LISTENER_ONGOING;
                            }
                            event.setWordStatus(status);
                            events.add(event);
                        }
                    }
                    SenseDataHolder.resetWordDataHolder();
                    //publish the data
                    if (events.size() > 0 && LocalRegistry.isEnrolled(context)) {
                        String user = LocalRegistry.getUsername(context);
                        String deviceId = LocalRegistry.getDeviceId(context);
                        JSONArray jsonArray = new JSONArray();
                        for (Event event : events) {
                            event.setOwner(user);
                            event.setDeviceId(deviceId);
                            jsonArray.put(new JSONObject().put("event", event.getEvent()));
                        }

                        MQTTTransportHandler mqttTransportHandler = AndroidSenseMQTTHandler.getInstance(context);
                        if (!mqttTransportHandler.isConnected()) {
                            mqttTransportHandler.connect();
                        }
                        String topic = LocalRegistry.getTenantDomain(context) + "/" + SenseConstants.DEVICE_TYPE + "/" + deviceId + "/data";
                        mqttTransportHandler.publishDeviceData(user, deviceId, jsonArray.toString(), topic);

                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Json Data Parsing Exception", e);
                } catch (TransportHandlerException e) {
                    Log.e(TAG, "Data Publish Failed", e);
                }
            }
        };
        Thread dataUploaderThread = new Thread(runnable);
        dataUploaderThread.start();
        return Service.START_NOT_STICKY;
    }
}