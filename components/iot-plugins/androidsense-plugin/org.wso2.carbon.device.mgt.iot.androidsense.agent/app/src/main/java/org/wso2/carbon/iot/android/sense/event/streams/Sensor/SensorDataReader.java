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
package org.wso2.carbon.iot.android.sense.event.streams.Sensor;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.wso2.carbon.iot.android.sense.event.streams.DataReader;
import org.wso2.carbon.iot.android.sense.realtimeviewer.sensorlisting.SupportedSensors;
import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;


/**
 * This is used to retrieve the sensor data.
 */
public class SensorDataReader extends DataReader implements SensorEventListener {
    private SensorManager mSensorManager;
    private Map<String, SensorData> senseDataStruct = new HashMap<>();
    private Vector<SensorData> sensorVector = new Vector<>();
    Context ctx;
    private List<Sensor> sensorList = new ArrayList<>();
    private SupportedSensors supportedSensors = SupportedSensors.getInstance();
    private static final String TAG = SensorDataReader.class.getName();

//    private float last_x, last_y, last_z;
//    private long lastUpdate;
//    String xTurnAxis;
//    private float speed;
//    private float x,y,z;


    public SensorDataReader(Context context) {
        ctx = context;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SupportedSensors.SELECTED_SENSORS, Context
                .MODE_MULTI_PROCESS);
        Set<String> selectedSet = sharedPreferences.getStringSet(SupportedSensors.SELECTED_SENSORS_BY_USER, null);
        mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        selectedSensorList(selectedSet);
        for (Sensor sensor : sensorList) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    private void collectSensorData() {
        for (Sensor sensor : sensorList) {
            try {
                if (senseDataStruct.containsKey(sensor.getName())) {
                    SensorData sensorInfo = senseDataStruct.get(sensor.getName());
                    sensorVector.add(sensorInfo);
                    Log.d(TAG, "Sensor Name " + sensor.getName() + ", Type " + sensor.getType() + " " +
                            ", sensorValue :" + sensorInfo.getSensorValues());
                }
            } catch (Throwable e) {
                Log.d(TAG, "error on sensors");
            }
        }
        mSensorManager.unregisterListener(this);
    }

    public Vector<SensorData> getSensorData() {
        try {
            TimeUnit.MILLISECONDS.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, e.getMessage());
        }
        collectSensorData();
        return sensorVector;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        senseDataStruct.put(event.sensor.getName(), new SensorData(event));

//        Sensor mySensor = event.sensor;
//
//        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//             x = event.values[0];
//             y = event.values[1];
//             z = event.values[2];
//
//
//            if(Round(x,4)>10.0000){
//                Log.d("sensor", "X Right axis: " + x);
//                xTurnAxis = "Right";
//            }
//            else if(Round(x,4)<-10.0000){
//                Log.d("sensor", "X Left axis: " + x);
//                xTurnAxis = "Left";
//
//
//            }
//
//            long curTime = System.currentTimeMillis();
//
//            long diffTime = (curTime - lastUpdate);
//            lastUpdate = curTime;
//
//            speed = Math.abs(x+y+z - last_x - last_y - last_z)/ diffTime * 10000;
//
//            Log.d("ADebugTag2", "DevSpeed Sensor: " + Float.toString(speed));
//
//            last_x = x;
//            last_y = y;
//            last_z = z;
//
//        }

//        Event events = new Event();
//        events.setSpeed(speed);
//        events.setTurns(xTurnAxis);

    }


//    public static float Round(float Rval, int Rpl) {
//        float p = (float)Math.pow(10,Rpl);
//        Rval = Rval * p;
//        float tmp = Math.round(Rval);
//        Log.d("round", "round: " + tmp/p);
//
//        return tmp/p;
//    }

    @Override
    public void run() {
        Log.d(TAG, "running -sensorDataMap");
        Vector<SensorData> sensorDetails = getSensorData();
        for (SensorData data : sensorDetails) {
            SenseDataHolder.getSensorDataHolder().add(data);
        }
    }



    public void selectedSensorList(Set<String> set) {
        if (set != null) {
            String[] sensorsSet = set.toArray(new String[set.size()]);
            for (String s : sensorsSet) {
                sensorList.add(mSensorManager.getDefaultSensor(supportedSensors.getType(s.toLowerCase())));
            }
        }
    }

}
