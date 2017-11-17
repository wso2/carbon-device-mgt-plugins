/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.iot.android.sense.realtimeviewer.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.wso2.carbon.iot.android.sense.realtimeviewer.datastore.TempStore;
import org.wso2.carbon.iot.android.sense.realtimeviewer.view.adaptor.SensorViewAdaptor;

/**
 * This class is to detect the sensor change event and update the sensor array list.
 * And update the view adaptor which is used to show the sensors list in the Android List view.
 */
public class RealTimeSensorChangeReceiver extends BroadcastReceiver {

    SensorViewAdaptor adaptor;

    public void updateOnChange(SensorViewAdaptor adaptor) {
        this.adaptor = adaptor;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        TempStore.sensorArrayList.clear();
        TempStore.sensorArrayList.addAll(TempStore.sensorDataMap.values());
    }
}
