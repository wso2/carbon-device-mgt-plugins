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

package org.wso2.carbon.iot.android.sense.event.streams;

import android.content.Context;
import org.wso2.carbon.iot.android.sense.event.streams.location.LocationDataReader;
import org.wso2.carbon.iot.android.sense.event.streams.sensor.SensorDataReader;
import org.wso2.carbon.iot.android.sense.event.streams.speed.SpeedDataReader;
import org.wso2.carbon.iot.android.sense.event.streams.audio.AudioDataReader;

/**
 * This class triggered by service to collect the sensor data.
 */
public class SenseDataCollector {
    public enum DataType {
        SENSOR, LOCATION,SPEED, AUDIO
    }

    public SenseDataCollector(Context ctx, DataType dt) {
        DataReader dr = null;
        switch (dt) {
            case SENSOR:
                dr = new SensorDataReader(ctx);
                break;
            case LOCATION:
                dr = new LocationDataReader(ctx);
                break;
            case SPEED:
                dr = new SpeedDataReader(ctx);
                break;
            case AUDIO:
                dr = new AudioDataReader(ctx);
                break;

        }
        if (dr != null) {
            Thread DataCollector = new Thread(dr);
            DataCollector.start();
        }
    }
}
