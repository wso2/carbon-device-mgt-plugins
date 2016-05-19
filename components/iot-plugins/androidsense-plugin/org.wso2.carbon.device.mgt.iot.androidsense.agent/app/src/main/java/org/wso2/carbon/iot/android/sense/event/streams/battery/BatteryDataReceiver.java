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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;

/**
 * Whenever the battery level changes This receiver will be triggered.
 */
public class BatteryDataReceiver extends BroadcastReceiver {

    /**
     * when the data is retreived then its added to a inmemory map.
     *
     * @param context of the reciever.
     * @param intent  of the reciver
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        SenseDataHolder.getBatteryDataHolder().add(new BatteryData(intent));
    }

}
