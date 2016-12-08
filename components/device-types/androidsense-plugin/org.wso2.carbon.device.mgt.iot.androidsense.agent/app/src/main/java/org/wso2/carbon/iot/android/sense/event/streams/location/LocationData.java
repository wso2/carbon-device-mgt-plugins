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
package org.wso2.carbon.iot.android.sense.event.streams.location;

import java.util.Date;

/**
 * This defines the data structure of the location data that is been collected.
 */
public class LocationData {
    private double latitude; // latitude
    private double longitude; // longitude
    private long timestamp;

    LocationData(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        timestamp = new Date().getTime();

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public void setTimeStamp(long timeStamp) {
        timestamp = timeStamp;
    }
}
