/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.iot.android.sense.bmonitor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Map;

import agent.sense.android.iot.carbon.wso2.org.wso2_senseagent.R;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.easycursor.objectcursor.EasyObjectCursor;

public class BeaconMonitoringActivity extends AppCompatActivity {

    private BluetoothLeScanner mScanner;
    private BluetoothUtils mBluetoothUtils;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothLeDeviceStore mDeviceStore;

    protected ListView mList;

    public static final int MINIMUM_DISTANCE = -70;

    public static Map itemMap;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_monitoring);

        itemMap = new HashMap<String, String>();
        itemMap.put("DC:5F:BD:68:88:D5", "Noodles");
        itemMap.put("EF:0F:50:D5:BA:A1", "Vegetables");
        itemMap.put("FA:F2:CF:84:C2:F7", "Oil");

        mList = (ListView) this.findViewById(android.R.id.list);

        mDeviceStore = new BluetoothLeDeviceStore();
        mBluetoothUtils = new BluetoothUtils(this);
        mScanner = new BluetoothLeScanner(mLeScanCallback, mBluetoothUtils);
        startScan();
    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            final BluetoothLeDevice deviceLe = new BluetoothLeDevice(device, rssi, scanRecord, System.currentTimeMillis());
            if(deviceLe!= null && deviceLe.getName()!= null && !deviceLe.getName().equals("Unknown Device")){
                mDeviceStore.addDevice(deviceLe);

                if(deviceLe.getRssi() > MINIMUM_DISTANCE){
                    Object[] objects = new Object[4];
                    objects[0] = deviceLe.getName();
                    objects[1] = deviceLe.getAddress();
                    objects[2] = deviceLe.getRssi();
                    objects[3] = itemMap.get(device.getAddress());

                    new SendToSiddi().execute(objects);
                }


                final EasyObjectCursor<BluetoothLeDevice> c = mDeviceStore.getDeviceCursor();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLeDeviceListAdapter.swapCursor(c);
                    }
                });
            }
        }
    };

    private void startScan() {

        mLeDeviceListAdapter = new LeDeviceListAdapter(this, mDeviceStore.getDeviceCursor());
        mList.setAdapter(mLeDeviceListAdapter);

        final boolean mIsBluetoothOn = mBluetoothUtils.isBluetoothOn();
        final boolean mIsBluetoothLePresent = mBluetoothUtils.isBluetoothLeSupported();
        mBluetoothUtils.askUserToEnableBluetoothIfNeeded();
        if (mIsBluetoothOn && mIsBluetoothLePresent) {
            mScanner.scanLeDevice(-1, true);
            invalidateOptionsMenu();
        }
    }


    public class SendToSiddi extends AsyncTask<Object, Object, Void>{

        @Override
        protected Void doInBackground(Object... objects) {

            return null;
        }
    }
}
