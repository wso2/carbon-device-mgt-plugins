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
import android.content.SharedPreferences;
import android.util.Log;

import org.wso2.carbon.iot.android.sense.constants.SenseConstants;
import org.wso2.carbon.iot.android.sense.data.publisher.mqtt.transport.MQTTTransportHandler;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This is used to store the values in either in memory or in shared preferences.
 */
public class LocalRegistry {

    private static final String SENSE_SHARED_PREFERENCES = "senseSharedPreferences";
    private static final String USERNAME_KEY = "usernameKey";
    private static final String DEVICE_ID_KEY = "deviceIdKey";
    private static final String SERVER_HOST_KEY = "serverHostKey";
    private static final String ACCESS_TOKEN_KEY = "accessTokenKey";
    private static final String REFRESH_TOKEN_KEY = "refreshTokenKey";
    private static final String MQTT_PORT_KEY = "mqttPort";
    private static boolean exists = false;
    private static String username;
    private static String deviceId;
    private static String serverURL;
    private static MQTTTransportHandler mqttTransportHandler;
    private static String accessToken;
    private static String refreshToken;
    private static int mqttPort;

    public static boolean isExist(Context context) {
        if (!exists) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            String username = sharedpreferences.getString(USERNAME_KEY, "");
            String deviceId = sharedpreferences.getString(DEVICE_ID_KEY, "");
            exists = (username != null && !username.isEmpty() && deviceId != null && !deviceId.isEmpty());
        }
        return exists;
    }

    public static void setExist(boolean status) {
        exists = status;
    }


    public static void addUsername(Context context, String username) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(USERNAME_KEY, username);
        editor.commit();
        LocalRegistry.username = username;
    }

    public static String getUsername(Context context) {
        if (LocalRegistry.username == null || username.isEmpty()) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            LocalRegistry.username = sharedpreferences.getString(USERNAME_KEY, "");
        }
        return LocalRegistry.username;
    }

    public static void removeUsername(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.remove(USERNAME_KEY);
        editor.commit();
        LocalRegistry.username = null;
    }

    public static void addDeviceId(Context context, String deviceId) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(DEVICE_ID_KEY, deviceId);
        editor.commit();
        LocalRegistry.deviceId = deviceId;
    }

    public static void removeDeviceId(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(DEVICE_ID_KEY);
        editor.clear();
        editor.commit();
        LocalRegistry.deviceId = null;
    }

    public static String getDeviceId(Context context) {
        if (LocalRegistry.deviceId == null || LocalRegistry.deviceId.isEmpty()) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            LocalRegistry.deviceId = sharedpreferences.getString(DEVICE_ID_KEY, "");
        }
        return LocalRegistry.deviceId;
    }

    public static void addServerURL(Context context, String host) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(SERVER_HOST_KEY, host);
        editor.commit();
        LocalRegistry.serverURL = host;
    }

    public static void removeServerURL(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(SERVER_HOST_KEY);
        editor.clear();
        editor.commit();
        LocalRegistry.serverURL = null;
    }

    public static String getServerURL(Context context) {
        if (LocalRegistry.serverURL == null || LocalRegistry.serverURL.isEmpty()) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            LocalRegistry.serverURL = sharedpreferences.getString(SERVER_HOST_KEY, "");
        }
        return LocalRegistry.serverURL;
    }

    public static void addAccessToken(Context context, String accessToken) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, accessToken);
        editor.commit();
        LocalRegistry.accessToken = accessToken;
    }

    public static void removeAccessToken(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(ACCESS_TOKEN_KEY);
        editor.clear();
        editor.commit();
        LocalRegistry.accessToken = null;
    }

    public static String getAccessToken(Context context) {
        if (LocalRegistry.accessToken == null || LocalRegistry.accessToken.isEmpty()) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            LocalRegistry.accessToken = sharedpreferences.getString(ACCESS_TOKEN_KEY, "");
        }
        return LocalRegistry.accessToken;
    }

    public static void addRefreshToken(Context context, String refreshToken) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(REFRESH_TOKEN_KEY, refreshToken);
        editor.commit();
        LocalRegistry.refreshToken = refreshToken;
    }

    public static void removeRefreshToken(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(REFRESH_TOKEN_KEY);
        editor.clear();
        editor.commit();
        LocalRegistry.refreshToken = null;
    }

    public static String getRefreshToken(Context context) {
        if (LocalRegistry.refreshToken == null || LocalRegistry.refreshToken.isEmpty()) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            LocalRegistry.refreshToken = sharedpreferences.getString(REFRESH_TOKEN_KEY, "");
        }
        return LocalRegistry.refreshToken;
    }

    public static void addMqttPort(Context context, int port) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(MQTT_PORT_KEY, port);
        editor.commit();
        LocalRegistry.mqttPort = port;
    }

    public static void removeMqttPort(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(MQTT_PORT_KEY);
        editor.clear();
        editor.commit();
        LocalRegistry.mqttPort = 0;
    }

    public static int getMqttPort(Context context) {
        if (LocalRegistry.mqttPort == 0) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(SENSE_SHARED_PREFERENCES, Context.MODE_PRIVATE);
            LocalRegistry.mqttPort = sharedpreferences.getInt(MQTT_PORT_KEY, SenseConstants.MQTT_BROKER_PORT);
        }
        return LocalRegistry.mqttPort;
    }

    public static String getServerHost(Context context) {

        URL url = null;
        String urlString = getServerURL(context);
        try {
            url = new URL(urlString);
            return url.getHost();
        } catch (MalformedURLException e) {
            Log.e("Host ", "Invalid urlString :" + urlString);
            return null;
        }
    }

}
