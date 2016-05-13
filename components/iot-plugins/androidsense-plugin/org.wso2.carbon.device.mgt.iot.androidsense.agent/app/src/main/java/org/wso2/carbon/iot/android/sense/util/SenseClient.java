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
import org.wso2.carbon.iot.android.sense.util.dto.RegisterInfo;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;

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
    public RegisterInfo register(String username, String password, String deviceId, android.os.Handler mUiHandler) {
        Map<String, String> response = registerWithTimeWait(username, password, deviceId);
        String responseStatus = response.get("status");
        RegisterInfo registerInfo = new RegisterInfo();
        if (responseStatus.trim().contains(SenseConstants.Request.REQUEST_SUCCESSFUL)) {
            registerInfo.setMsg("Login Succesful");
            registerInfo.setIsRegistered(true);
            return registerInfo;
        } else {
            registerInfo.setMsg("Authentication failed, please check your credentials and try again.");
            registerInfo.setIsRegistered(false);
            return registerInfo;
        }
    }

    public Map<String, String> registerWithTimeWait(String username, String password, String deviceId) {
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
        return null;
    }

}
