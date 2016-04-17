/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.iot.android.sense;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import org.wso2.carbon.iot.android.sense.data.publisher.DataPublisherReceiver;
import org.wso2.carbon.iot.android.sense.data.publisher.mqtt.AndroidSenseMQTTHandler;
import org.wso2.carbon.iot.android.sense.data.publisher.mqtt.transport.MQTTTransportHandler;
import org.wso2.carbon.iot.android.sense.event.SenseScheduleReceiver;
import org.wso2.carbon.iot.android.sense.realtimeviewer.ActivitySelectSensor;
import org.wso2.carbon.iot.android.sense.realtimeviewer.sensorlisting.AvailableSensorsInDevice;
import org.wso2.carbon.iot.android.sense.realtimeviewer.sensorlisting.SupportedSensors;
import org.wso2.carbon.iot.android.sense.util.LocalRegistry;
import org.wso2.carbon.iot.android.sense.util.SenseClient;
import org.wso2.carbon.iot.android.sense.util.SenseUtils;
import agent.sense.android.iot.carbon.wso2.org.wso2_senseagent.R;


/**
 * A login screen that offers to register the device.
 */
public class RegisterActivity extends Activity {

    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mHostView;
    private EditText mMqttPortView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSharedPreferences(SupportedSensors.SELECTED_SENSORS, 0).edit().clear().apply();

        if (LocalRegistry.isExist(getApplicationContext())) {
            Intent intent = new Intent(getApplicationContext(), ActivitySelectSensor.class);
            startActivity(intent);
        }
        setContentView(R.layout.activity_register);
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mHostView = (EditText) findViewById(R.id.hostname);
        mMqttPortView = (EditText) findViewById(R.id.mqttPort);
        AvailableSensorsInDevice availableSensorsInDevice = new AvailableSensorsInDevice(getApplicationContext());
        availableSensorsInDevice.setContent();

        Button deviceRegisterButton = (Button) findViewById(R.id.device_register_button);


        deviceRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    public void attemptLogin() {
        showProgress(true);
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String hostname = mHostView.getText().toString();
        String mqttPort = mMqttPortView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password)) {
            // mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            //cancel = true;
        }
        // Check for a valid username .
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }
        if (TextUtils.isEmpty(username)) {
            mHostView.setError(getString(R.string.error_field_required));
            focusView = mHostView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            SenseClient client = new SenseClient(getApplicationContext());
            LocalRegistry.addServerURL(getBaseContext(), hostname);
            String deviceId = SenseUtils.generateDeviceId(getBaseContext(), getContentResolver());
            boolean registerStatus = client.register(username, password, deviceId);
            if (registerStatus) {
                LocalRegistry.addUsername(getApplicationContext(), username);
                LocalRegistry.addDeviceId(getApplicationContext(), deviceId);
                LocalRegistry.addMqttPort(getApplicationContext(), Integer.parseInt(mqttPort));
                MQTTTransportHandler mqttTransportHandler = AndroidSenseMQTTHandler.getInstance(this);
                if (!mqttTransportHandler.isConnected()) {
                    mqttTransportHandler.connect();
                }
                SenseScheduleReceiver senseScheduleReceiver = new SenseScheduleReceiver();
                senseScheduleReceiver.clearAbortBroadcast();
                senseScheduleReceiver.onReceive(this, null);

                DataPublisherReceiver dataUploaderReceiver = new DataPublisherReceiver();
                dataUploaderReceiver.clearAbortBroadcast();
                dataUploaderReceiver.onReceive(this, null);

                Intent intent = new Intent(getApplicationContext(), ActivitySelectSensor.class);
                startActivity(intent);
            }
            showProgress(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}

