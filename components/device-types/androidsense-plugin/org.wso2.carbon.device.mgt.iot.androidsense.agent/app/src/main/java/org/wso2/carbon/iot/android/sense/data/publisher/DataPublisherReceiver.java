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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This creates and AlarmManagerService that triggers the data uploader service with a 30 seconds interval.
 */
public class DataPublisherReceiver extends BroadcastReceiver {
    private static int ALARM_INTERVAL = 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.i("Data Publisher", "triggered");
        Intent i = new Intent(context, DataPublisherService.class);
        PendingIntent pending = PendingIntent.getService(context, 0, i, 0);
        service.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), ALARM_INTERVAL, pending);
    }
}