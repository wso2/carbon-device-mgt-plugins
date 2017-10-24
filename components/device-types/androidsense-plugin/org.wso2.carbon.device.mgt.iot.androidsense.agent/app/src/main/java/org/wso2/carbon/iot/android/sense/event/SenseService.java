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
package org.wso2.carbon.iot.android.sense.event;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.wso2.carbon.iot.android.sense.event.streams.SenseDataCollector;
import org.wso2.carbon.iot.android.sense.util.LocalRegistry;
import org.wso2.carbon.iot.android.sense.util.SenseDataReceiverManager;
import org.wso2.carbon.iot.android.sense.util.SenseWakeLock;

/**
 * This service caters to initiate the data collection.
 */
public class SenseService extends Service {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        SenseWakeLock.acquireWakeLock(this);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        if (!LocalRegistry.isExist(context)) return Service.START_NOT_STICKY;
        //Below triggers the data collection for sensors,location and battery.
        SenseDataCollector Sensor = new SenseDataCollector(this, SenseDataCollector.DataType.SENSOR);
        SenseDataCollector Location = new SenseDataCollector(this, SenseDataCollector.DataType.LOCATION);
        SenseDataCollector speed = new SenseDataCollector(this, SenseDataCollector.DataType.SPEED);
        SenseDataCollector audio = new SenseDataCollector(this, SenseDataCollector.DataType.AUDIO);
        SenseDataReceiverManager.registerBatteryDataReceiver(this);
        SenseDataReceiverManager.registerScreenDataReceiver(this);
        SenseDataReceiverManager.registerCallDataReceiver(this);
        SenseDataReceiverManager.registerActivityDataReceiver(this);
        SenseDataReceiverManager.registerSmsDataReceiver(this);
        SenseDataReceiverManager.registerAppDataReceiver(this);
        SenseDataReceiverManager.registerNetworkDataReader(this);
        //service will not be stopped until we manually stop the service
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        SenseDataReceiverManager.unregisterBatteryDataReceiver(this);
        SenseDataReceiverManager.unregisterScreenDataReceiver(this);
        SenseDataReceiverManager.unregisterCallDataReceiver(this);
        SenseDataReceiverManager.unregisterActivityDataReceiver(this);
        SenseDataReceiverManager.unregisterSmsDataReceiver(this);
        SenseDataReceiverManager.unregisterAppDataReceiver(this);
        SenseDataReceiverManager.unregisterNetworkDataReader();

        SenseWakeLock.releaseCPUWakeLock();
        super.onDestroy();
    }
}
