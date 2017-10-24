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

package org.wso2.carbon.iot.android.sense.event.streams.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;

import java.util.Date;

/**
 * Class to read data sent and received by the device.
 */
public class NetworkDataReader extends AsyncTask<Void, Void, Long> {

    private NetworkData networkData;
    private Context context;
    private Handler mHandler = new Handler();
    private long mStartRX = 0;
    private long mStartTX = 0;
    private final String WIFI = "WIFI";
    private final String MOBILE = "MOBILE";
    private String connectionType;

    public NetworkDataReader(Context context) {
        this.context = context;
    }

    @Override
    protected Long doInBackground(Void... voids) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkData = new NetworkData();

        if (getConnectionType(connectivityManager, ConnectivityManager.TYPE_WIFI)) {
            connectionType = WIFI;
        } else if (getConnectionType(connectivityManager, ConnectivityManager.TYPE_MOBILE)) {
            connectionType = MOBILE;
        }

        mStartRX = TrafficStats.getTotalRxBytes();
        mStartTX = TrafficStats.getTotalTxBytes();
        if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {
            Log.e("ERROR", "Not connected.");
        } else {
            mHandler.postDelayed(mRunnable, 10000);
        }
        return null;
    }

    /**
     * Collect data sent and received with in 10 second time frames.
     */
    private final Runnable mRunnable = new Runnable() {
        public void run() {
            long rxBytes = TrafficStats.getTotalRxBytes() - mStartRX;
            long txBytes = TrafficStats.getTotalTxBytes() - mStartTX;
            Log.i("Usage: ", String.valueOf(rxBytes) + " " + String.valueOf(txBytes) + " " + System.currentTimeMillis());
            networkData.setType(connectionType);
            networkData.setTimeStamp(new Date().getTime());
            networkData.setDataSent(txBytes);
            networkData.setDataReceived(rxBytes);
            SenseDataHolder.getNetworkDataHolder().add(networkData);
            mHandler.postDelayed(mRunnable, 10000);
        }
    };

    /**
     * Get the type of the connection currently have.
     */
    private boolean getConnectionType(ConnectivityManager manager, Integer type) {
        NetworkInfo networkInfo = manager.getNetworkInfo(type);
        return networkInfo.isConnected();
    }
}
