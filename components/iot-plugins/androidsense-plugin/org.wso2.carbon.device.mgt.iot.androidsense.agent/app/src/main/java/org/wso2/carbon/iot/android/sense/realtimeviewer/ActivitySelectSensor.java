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
package org.wso2.carbon.iot.android.sense.realtimeviewer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import org.wso2.carbon.iot.android.sense.RegisterActivity;
import org.wso2.carbon.iot.android.sense.data.publisher.DataPublisherReceiver;
import org.wso2.carbon.iot.android.sense.data.publisher.DataPublisherService;
import org.wso2.carbon.iot.android.sense.event.SenseScheduleReceiver;
import org.wso2.carbon.iot.android.sense.event.SenseService;
import org.wso2.carbon.iot.android.sense.realtimeviewer.datastore.TempStore;
import org.wso2.carbon.iot.android.sense.realtimeviewer.event.RealTimeSensorChangeReceiver;
import org.wso2.carbon.iot.android.sense.realtimeviewer.event.realtimesensor.RealTimeSensorReader;
import org.wso2.carbon.iot.android.sense.realtimeviewer.sensorlisting.SupportedSensors;
import org.wso2.carbon.iot.android.sense.realtimeviewer.view.adaptor.SensorViewAdaptor;
import org.wso2.carbon.iot.android.sense.realtimeviewer.view.sensor.selector.SelectSensorDialog;
import org.wso2.carbon.iot.android.sense.speech.detector.WordRecognitionActivity;
import org.wso2.carbon.iot.android.sense.beacon.MonitoringActivity;

import org.wso2.carbon.iot.android.sense.util.LocalRegistry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.altbeacon.beacon.BeaconManager;

import agent.sense.android.iot.carbon.wso2.org.wso2_senseagent.R;

/**
 * Activity for selecting sensors available in the device
 */

public class ActivitySelectSensor extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SelectSensorDialog.SensorListListener {

    private SharedPreferences sharedPreferences;
    private SelectSensorDialog sensorDialog = new SelectSensorDialog();
    private Set<String> selectedSensorSet = new HashSet<>();
    private ListView listView;
    private SensorManager sensorManager;
    private ArrayList<Sensor> sensors = new ArrayList<>();
    private EditText sessionIdText;
    private RealTimeSensorReader sensorReader = null;
    private RealTimeSensorChangeReceiver realTimeSensorChangeReceiver = new RealTimeSensorChangeReceiver();
    private SupportedSensors supportedSensors = SupportedSensors.getInstance();
    protected static final String TAG =  ActivitySelectSensor.class.getName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_select_sensor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sessionIdText = (EditText) findViewById(R.id.sessionId);
        sessionIdText.setCursorVisible(false);

        listView = (ListView) findViewById(R.id.senseListContainer);
        verifyBluetooth();

        registerReceiver(realTimeSensorChangeReceiver, new IntentFilter("sensorDataMap"));

        sessionIdText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionIdText.setCursorVisible(true);

            }
        });

        //Publish data
        FloatingActionButton fbtnPublishData = (FloatingActionButton) findViewById(R.id.publish);

        fbtnPublishData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Publishing data started", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                DataPublisherReceiver dataPublisherReceiver = new DataPublisherReceiver();
                dataPublisherReceiver.clearAbortBroadcast();
                dataPublisherReceiver.onReceive(getApplicationContext(), null);
            }
        });

        FloatingActionButton fbtnAddSensors = (FloatingActionButton) findViewById(R.id.addSensors);
        fbtnAddSensors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorDialog.show(getFragmentManager(), "Sensor List");
            }
        });

        FloatingActionButton fbtnSpeechRecongnizer = (FloatingActionButton) findViewById(R.id.speech);
        fbtnSpeechRecongnizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sessionId = sessionIdText.getText().toString();
                if (!sessionId.isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), WordRecognitionActivity.class);
                    intent.putExtra("sessionId", sessionId);
                    startActivity(intent);
                } else {
                    Toast.makeText(ActivitySelectSensor.this, "Please type a session id value", Toast.LENGTH_SHORT)
                            .show();
                }

            }
        });

        FloatingActionButton fbtnBeaconMonitor = (FloatingActionButton) findViewById(R.id.beacon);
        fbtnBeaconMonitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MonitoringActivity.class);
                    startActivity(intent);


            }
        });


        sharedPreferences = getSharedPreferences(SupportedSensors.SELECTED_SENSORS, 0);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_select_sensor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_deEnroll) {

            /**
             * unregister the sensors and broadcast receivers.
             * */
            unregisterSensors();
            unregisterReceivers();

            if (!LocalRegistry.isExist(getApplicationContext())) {
                Intent activity = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(activity);
            }
            LocalRegistry.setEnrolled(getApplicationContext(), false);
            LocalRegistry.removeUsername(getApplicationContext());
            LocalRegistry.removeDeviceId(getApplicationContext());
            LocalRegistry.removeServerURL(getApplicationContext());
            LocalRegistry.removeAccessToken(getApplicationContext());
            LocalRegistry.removeRefreshToken(getApplicationContext());
            LocalRegistry.removeMqttEndpoint(getApplicationContext());
            LocalRegistry.removeTenantDomain(getApplicationContext());
            LocalRegistry.setExist(false);
            //Stop the current running background services.
            stopService(new Intent(this, SenseService.class)); //Stop sensor reading service
            stopService(new Intent(this, DataPublisherService.class)); //Stop data uploader service

            Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
            registerActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(registerActivity);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.select) {
            sensorDialog.show(getFragmentManager(), "Sensor List");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    public void onDialogPositiveClick(SelectSensorDialog dialog) {

        Log.d("Selected sensors", dialog.getSet().toString());
        selectedSensorSet = dialog.getSet();
        update();
        unregisterSensors();

        SenseScheduleReceiver senseScheduleReceiver = new SenseScheduleReceiver();
        senseScheduleReceiver.clearAbortBroadcast();
        senseScheduleReceiver.onReceive(this, null);

        /**
         * Get the selected sensors
         * Register them
         * */
        SensorViewAdaptor adaptor1 = new SensorViewAdaptor(getApplicationContext(), TempStore.sensorArrayList);
        adaptor1.notifyDataSetChanged();

        sensorReader = new RealTimeSensorReader(this, adaptor1);
        getSensors();

        for (Sensor s : sensors) {
            sensorManager.registerListener(sensorReader, s, SensorManager.SENSOR_DELAY_NORMAL);
        }

        realTimeSensorChangeReceiver.updateOnChange(adaptor1);
        listView.setAdapter(adaptor1);

    }

    public void update() {
        Log.d("Update", "Set the values to Shared Preferences");

        TempStore.sensorArrayList.clear();
        TempStore.sensorDataMap.clear();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(SupportedSensors.SELECTED_SENSORS_BY_USER, selectedSensorSet);
        editor.apply();
    }

    public void getSensors() {
        sensors.clear();
        for (String sensor : selectedSensorSet.toArray(new String[selectedSensorSet.size()])) {
            sensors.add(sensorManager.getDefaultSensor(supportedSensors.getType(sensor.toLowerCase())));
        }
    }

    /**
     * This method will unregister all the registered sensors.
     */
    public void unregisterSensors() {
        if (sensors.size() > 0) {
            for (Sensor s : sensors) {
                System.out.println(s.getName() + " Unregistered!");
                sensorManager.unregisterListener(sensorReader, s);
            }
        }
    }


    /**
     * This method unregisters the real-time broadcast receiver.
     */
    public void unregisterReceivers() {
        unregisterReceiver(realTimeSensorChangeReceiver);
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }

                });
                builder.show();

            }
        } catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();

        }

    }
}
