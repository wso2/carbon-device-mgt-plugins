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

package org.wso2.carbon.iot.android.sense.event.streams.speed;

import org.wso2.carbon.iot.android.sense.event.streams.DataReader;
import org.wso2.carbon.iot.android.sense.event.streams.sensor.SensorData;
import org.wso2.carbon.iot.android.sense.realtimeviewer.sensorlisting.SupportedSensors;
import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import android.content.BroadcastReceiver;

import android.content.Intent;


public class SpeedDataReader extends DataReader implements SensorEventListener {

    SpeedData data;
    private SensorManager mSensorManager;
    private Map<String, SensorData> senseDataStruct = new HashMap<>();
    private Vector<SensorData> sensorVector = new Vector<>();
    private static final String TAG = SpeedDataReader.class.getName();
    private float last_x, last_y, last_z;
    private long lastUpdate;
    private String xTurnAxis;
    float speed;
    private float x,y,z;
    Context ctx;
    private List<Sensor> sensorList = new ArrayList<>();
    private SupportedSensors supportedSensors = SupportedSensors.getInstance();

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor devSensor = event.sensor;

        if (devSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
             x = event.values[0];
             y = event.values[1];
             z = event.values[2];

        }
    }

    public SpeedDataReader(Context context) {
        ctx = context;
        SharedPreferences sharedPreferences = ctx.getSharedPreferences(SupportedSensors.SELECTED_SENSORS, Context
                .MODE_MULTI_PROCESS);
        Set<String> selectedSet = sharedPreferences.getStringSet(SupportedSensors.SELECTED_SENSORS_BY_USER, null);
        mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        selectedSensorList(selectedSet);
        for (Sensor sensor : sensorList) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        LocalBroadcastManager.getInstance(ctx).registerReceiver(mMessageReceiver,
                new IntentFilter("speedUpdate"));

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


    public String getTurns() {

        if(Round(x,4)>10.0000){
            Log.d("sensor", "X Right axis: " + x);
            xTurnAxis = "Right";
            return xTurnAxis;
        }else if(Round(x,4)<-10.0000){
            Log.d("sensor", "X Left axis: " + x);
            xTurnAxis = "Left";
            return xTurnAxis;
        }else {
            xTurnAxis = "No Turns";

        }
        return xTurnAxis;
    }

    public float getSpeed(){

        return speed;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // can be safely ignored for this demo
    }

    public static float Round(float Rval, int Rpl) {
        float p = (float)Math.pow(10,Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        Log.d("round", "round: " + tmp/p);

        return tmp/p;
    }

    @Override
    public void run() {
        Log.d(TAG, "running - Device Speed");
        try {
            TimeUnit.MILLISECONDS.sleep(10000);
           // String trn = getTurns();
           // double spd = getSpeed();
            //if (trn != 0 && spd != 0) {
                data = new SpeedData(getSpeed(), getTurns());
                SenseDataHolder.getSpeedDataHolder().add(data);
                collectSensorData();

            //}
        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
            Log.e(TAG, " Speed Data Retrieval Failed");
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            speed = intent.getFloatExtra("speed",speed);

            Log.d("receiver", "Got message: " + speed);
        }
    };


}
