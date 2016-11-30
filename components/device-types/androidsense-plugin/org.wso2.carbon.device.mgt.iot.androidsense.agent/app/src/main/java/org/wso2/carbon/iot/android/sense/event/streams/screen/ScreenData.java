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

package org.wso2.carbon.iot.android.sense.event.streams.screen;

import android.content.Intent;

import java.util.Date;

public class ScreenData {
    private String action;
    private long timestamp;

    ScreenData(Intent intent) {
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            this.action = "on";
        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            this.action = "off";
        } else {
            this.action = "unknown";
        }
        this.timestamp = new Date().getTime();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
