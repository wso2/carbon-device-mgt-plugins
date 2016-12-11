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
package org.wso2.carbon.iot.android.sense.event.streams.battery;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;

import org.wso2.carbon.iot.android.sense.data.publisher.DataPublisherService;
import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;

/**
 * Whenever the battery level changes This receiver will be triggered.
 */
public class BatteryDataReceiver extends BroadcastReceiver {

    private final long ALARM_INTERVAL = 1000;
    /**
     * When the data is retrieved then its added to a in memory map.
     *
     * @param context of the receiver.
     * @param intent  of the receiver
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.i("Battery Data Receiver", "Triggered");
        Intent i = new Intent(context, BatteryReaderService.class);
        PendingIntent pending = PendingIntent.getService(context, 0, i, 0);
        service.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), ALARM_INTERVAL, pending);
    }

}
