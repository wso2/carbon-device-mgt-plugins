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

package org.wso2.carbon.iot.android.sense.beacon;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.RangeNotifier;
import android.widget.ListView;
import android.widget.TextView;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import java.util.ArrayList;
import java.util.Collection;
import agent.sense.android.iot.carbon.wso2.org.wso2_senseagent.R;

public class MonitoringActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = MonitoringActivity.class.getName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private ListView list = null;
    private BeaconAdapter adapter = null;
    private ArrayList<Beacon> arrayL = new ArrayList<>();
    private LayoutInflater inflater;

    private BeaconServiceUtility beaconUtill = null;
    private BeaconManager iBeaconManager = BeaconManager.getInstanceForApplication(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
        beaconUtill = new BeaconServiceUtility(this);
        list = (ListView) findViewById(R.id.list);
        adapter = new BeaconAdapter();
        list.setAdapter(adapter);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        beaconUtill.onStart(iBeaconManager, this);
    }

    @Override
    protected void onStop() {
        beaconUtill.onStop(iBeaconManager, this);
        super.onStop();
    }

    @Override
    public void onBeaconServiceConnect() {

        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> iBeacons, Region region) {

                arrayL.clear();
                arrayL.addAll((ArrayList<Beacon>) iBeacons);
               // adapter.notifyDataSetChanged();
            }

        });

        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.e("BeaconDetactorService", "didEnterRegion");
                // logStatus("I just saw an iBeacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.e("BeaconDetactorService", "didExitRegion");
                // logStatus("I no longer see an iBeacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.e("BeaconDetactorService", "didDetermineStateForRegion");
                // logStatus("I have just switched from seeing/not seeing iBeacons: " + state);
            }

        });

        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class BeaconAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (arrayL != null && arrayL.size() > 0)
                return arrayL.size();
            else
                return 0;
        }

        @Override
        public Beacon getItem(int arg0) {
            return arrayL.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                ViewHolder holder;

                if (convertView != null) {
                    holder = (ViewHolder) convertView.getTag();
                } else {
                    holder = new ViewHolder(convertView = inflater.inflate(R.layout.tupple_monitoring, null));
                }
                if (arrayL.get(position).getId1() != null)
                    holder.beacon_uuid.setText("UUID: " + arrayL.get(position).getServiceUuid());

                holder.beacon_major.setText("Major: " + arrayL.get(position).getId1());

                holder.beacon_minor.setText(", Minor: " + arrayL.get(position).getId2());

                holder.beacon_proximity.setText("Proximity: " + arrayL.get(position).getId3());

                holder.beacon_rssi.setText(", Rssi: " + arrayL.get(position).getRssi());

                holder.beacon_txpower.setText(", TxPower: " + arrayL.get(position).getTxPower());

                holder.beacon_range.setText("" + arrayL.get(position).getDistance());

            } catch (Exception e) {
                e.printStackTrace();
            }

            return convertView;
        }

        private class ViewHolder {
            private TextView beacon_uuid;
            private TextView beacon_major;
            private TextView beacon_minor;
            private TextView beacon_proximity;
            private TextView beacon_rssi;
            private TextView beacon_txpower;
            private TextView beacon_range;

            public ViewHolder(View view) {
                beacon_uuid = (TextView) view.findViewById(R.id.BEACON_uuid);
                beacon_major = (TextView) view.findViewById(R.id.BEACON_major);
                beacon_minor = (TextView) view.findViewById(R.id.BEACON_minor);
                beacon_proximity = (TextView) view.findViewById(R.id.BEACON_proximity);
                beacon_rssi = (TextView) view.findViewById(R.id.BEACON_rssi);
                beacon_txpower = (TextView) view.findViewById(R.id.BEACON_txpower);
                beacon_range = (TextView) view.findViewById(R.id.BEACON_range);

                view.setTag(this);
            }
        }

    }

}