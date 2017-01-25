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

package org.wso2.carbon.iot.android.sense.event.streams.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;

/**
 * Currently interested on package add and remove only. Can be extended for modification.
 */
public class ApplicationDataReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getData().toString().substring(8);
        ApplicationData appData;
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            appData = new ApplicationData(packageName, ApplicationData.Action.INSTALL);
        } else {
            // Removed
            appData = new ApplicationData(packageName, ApplicationData.Action.REMOVE);
        }
        SenseDataHolder.getApplicationDataHolder().add(appData);
    }
}
