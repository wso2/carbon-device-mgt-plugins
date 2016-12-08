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

package org.wso2.carbon.iot.android.sense.event.streams.data;

import java.util.Date;

public class NetworkData {

    //Mobile or Wifi
    private String DATA_TYPE;
    private long dataReceived;
    private long dataSent;
    private long timeStamp;

    public NetworkData(long received, long sent) {
        this.dataReceived = received;
        this.dataSent = sent;
        this.timeStamp = new Date().getTime();
    }

    public String getDataType() {
        return DATA_TYPE;
    }

    public long getDataReceived() {
        return dataReceived;
    }

    public void setDataReceived(long dataReceived) {
        this.dataReceived = dataReceived;
    }

    public long getDataSent() {
        return dataSent;
    }

    public void setDataSent(long dataSent) {
        this.dataSent = dataSent;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
