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

package org.wso2.carbon.iot.android.sense.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import org.wso2.carbon.iot.android.sense.event.streams.activity.ActivityReceiver;
import org.wso2.carbon.iot.android.sense.event.streams.application.ApplicationDataReceiver;
import org.wso2.carbon.iot.android.sense.event.streams.battery.BatteryDataReceiver;
import org.wso2.carbon.iot.android.sense.event.streams.call.CallDataReceiver;
import org.wso2.carbon.iot.android.sense.event.streams.data.NetworkDataReader;
import org.wso2.carbon.iot.android.sense.event.streams.screen.ScreenDataReceiver;
import org.wso2.carbon.iot.android.sense.event.streams.sms.SmsDataReceiver;

public class SenseDataReceiverManager {
    private static BroadcastReceiver batteryDataReceiver;

    private static BroadcastReceiver screenDataReceiver;

    private static BroadcastReceiver callDataReceiver;

    private static GoogleApiClient apiClient;

    private static SmsDataReceiver smsDataReceiver;

    private static ApplicationDataReceiver appDataReceiver;

    private static NetworkDataReader networkDataReader;

    private SenseDataReceiverManager() {

    }

    public static void registerBatteryDataReceiver(Context context) {
        if (batteryDataReceiver == null) {
            batteryDataReceiver = new BatteryDataReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_LOW);
            intentFilter.addAction(Intent.ACTION_BATTERY_OKAY);
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

            context.registerReceiver(batteryDataReceiver, intentFilter);
        }
    }

    public static void unregisterBatteryDataReceiver(Context context) {
        if (batteryDataReceiver != null) {
            context.unregisterReceiver(batteryDataReceiver);
            batteryDataReceiver = null;
        }
    }

    public static void registerScreenDataReceiver(Context context) {
        if (screenDataReceiver == null) {
            screenDataReceiver = new ScreenDataReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);

            context.registerReceiver(screenDataReceiver, intentFilter);
        }
    }

    public static void unregisterScreenDataReceiver(Context context) {
        if (screenDataReceiver != null) {
            context.unregisterReceiver(screenDataReceiver);
            screenDataReceiver = null;
        }
    }

    public static void registerCallDataReceiver(Context context) {
        if (callDataReceiver == null) {
            callDataReceiver = new CallDataReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);

            context.registerReceiver(callDataReceiver, intentFilter);
        }
    }

    public static void unregisterCallDataReceiver(Context context) {
        if (callDataReceiver != null) {
            context.unregisterReceiver(callDataReceiver);
            callDataReceiver = null;
        }
    }

    public static void registerActivityDataReceiver(Context context) {
        if (apiClient == null) {
            Intent intent = new Intent(context, ActivityReceiver.class);
            final PendingIntent pendingIntent = PendingIntent.getService(context, 888971, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            apiClient = new GoogleApiClient.Builder(context)
                    .addApi(ActivityRecognition.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(apiClient, ActivityReceiver.UPDATE_INTERVAL, pendingIntent);
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(apiClient, pendingIntent);
                        }
                    })
                    .build();

            apiClient.connect();
        }
    }

    public static void unregisterActivityDataReceiver(Context context) {
        if (apiClient != null) {
            apiClient.disconnect();
            apiClient = null;
        }
    }

    public static void registerSmsDataReceiver(Context context) {
        if (smsDataReceiver == null) {
            smsDataReceiver = new SmsDataReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
            intentFilter.addAction(Telephony.Sms.Intents.SMS_DELIVER_ACTION);
            context.registerReceiver(smsDataReceiver, intentFilter);
        }
    }

    public static void unregisterSmsDataReceiver(Context context) {
        if (smsDataReceiver != null) {
            context.unregisterReceiver(smsDataReceiver);
            smsDataReceiver = null;
        }
    }

    public static void registerAppDataReceiver(Context context) {
        if (appDataReceiver == null) {
            appDataReceiver = new ApplicationDataReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            context.registerReceiver(appDataReceiver, intentFilter);
        }
    }

    public static void unregisterAppDataReceiver(Context context) {
        if (appDataReceiver != null) {
            context.unregisterReceiver(appDataReceiver);
            appDataReceiver = null;
        }
    }

    public static void registerNetworkDataReader(Context context) {
        if (networkDataReader == null) {
            networkDataReader = new NetworkDataReader(context);
            networkDataReader.execute();
        }
    }

    public static void unregisterNetworkDataReader() {
        if (networkDataReader != null) {
            networkDataReader.cancel(true);
        }
        networkDataReader = null;
    }


}
