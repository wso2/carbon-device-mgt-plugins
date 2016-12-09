/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.iot.android.sense.event.streams.battery;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;

public class BatteryReaderService extends IntentService {

    private Context context;

    public BatteryReaderService() {
        super("BatteryReaderService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent intent1 = registerReceiver(null, intentFilter);

        Log.i("Battery Data", String.valueOf(intent1.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)));
        if (Intent.ACTION_BATTERY_OKAY.equals(intent.getAction())) {
            SenseDataHolder.getBatteryDataHolder().add(new BatteryData(BatteryData.State.OK));
        } else if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
            SenseDataHolder.getBatteryDataHolder().add(new BatteryData(BatteryData.State.LOW));
        } else {
            SenseDataHolder.getBatteryDataHolder().add(new BatteryData(intent1));
        }
    }
}
