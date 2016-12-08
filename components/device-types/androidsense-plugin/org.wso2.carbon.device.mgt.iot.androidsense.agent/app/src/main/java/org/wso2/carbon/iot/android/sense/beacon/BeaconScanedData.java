/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.iot.android.sense.beacon;

import java.util.Date;


public class BeaconScanedData {


    private int beaconMajor;// Major
    private int beaconMinor;//Minor
    private String beaconProximity;// Proximity
    private int beaconUuid;// Uuid
    private long timestamp;// Timestamp

    BeaconScanedData(int beaconMajor, int beaconMinor,String beaconProximity,int beaconUuid) {
        this.beaconMajor = beaconMajor;
        this.beaconMinor = beaconMinor;
        this.beaconProximity = beaconProximity;
        this.beaconUuid = beaconUuid;
        timestamp = new Date().getTime();

    }

    public int getBeaconMajor() {
        return beaconMajor;
    }

    public void setBeaconMajor(int beaconMajor) {
        this.beaconMajor = beaconMajor;
    }

    public int getBeaconMinor() {
        return beaconMinor;
    }

    public void setBeaconMinor(int beaconMinor) {
        this.beaconMinor = beaconMinor;
    }

    public String getBeaconProximity() {
        return beaconProximity;
    }

    public void setBeaconProximity(String beaconProximity) {
        this.beaconProximity = beaconProximity;
    }

    public int getBeaconUuid() {
        return beaconUuid;
    }

    public void setBeaconUuid(int beaconUuid) {
        this.beaconUuid = beaconUuid;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public void setTimeStamp(long timeStamp) {
        timestamp = timeStamp;
    }
}
