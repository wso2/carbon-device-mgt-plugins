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
package org.wso2.carbon.iot.android.sense.util;


import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.wso2.carbon.iot.android.sense.constants.SenseConstants;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * This Client is used for http communication with the server.
 */
public class SenseClient {
    private final static String TAG = "SenseService Client";

    private Context context;

    public SenseClient(Context context) {
        this.context = context;
    }

    /**
     * Enroll the device.
     *
     * @param username
     * @param password
     * @param deviceId
     * @return
     */
    public boolean register(String username, String password, String deviceId) {
        Map<String, String> response = registerWithTimeWait(username, password, deviceId);
        String responseStatus = response.get("status");
        if (responseStatus.trim().contains(SenseConstants.Request.REQUEST_SUCCESSFUL)) {
            Toast.makeText(context, "Device Registered", Toast.LENGTH_LONG).show();
            return true;
        } else if (responseStatus.trim().contains(SenseConstants.Request.REQUEST_CONFLICT)) {
            Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(context, "Authentication failed, please check your credentials and try again.", Toast
                    .LENGTH_LONG).show();

            return false;
        }
    }

    public Map<String, String> registerWithTimeWait(String username, String password, String deviceId) {
        for (int i = 1; i <= SenseConstants.Request.MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");
            try {
                SenseClientAsyncExecutor senseClientAsyncExecutor = new SenseClientAsyncExecutor(context);
                String endpoint = LocalRegistry.getServerURL(context);
                senseClientAsyncExecutor.execute(username, password, deviceId, endpoint);
                Map<String, String> response = senseClientAsyncExecutor.get();
                if (response != null) {
                    return response;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e("Send Sensor Data", "Thread Interruption for endpoint " + LocalRegistry.getServerURL(context));
            } catch (ExecutionException e) {
                Log.e("Send Sensor Data", "Failed to push data to the endpoint " + LocalRegistry.getServerURL(context));
            }
        }
        return null;
    }

}
