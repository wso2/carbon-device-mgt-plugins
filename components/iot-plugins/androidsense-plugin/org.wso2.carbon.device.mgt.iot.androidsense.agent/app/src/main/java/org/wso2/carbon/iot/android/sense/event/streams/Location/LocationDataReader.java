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
package org.wso2.carbon.iot.android.sense.event.streams.Location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import org.wso2.carbon.iot.android.sense.event.streams.DataReader;
import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;
import java.util.concurrent.TimeUnit;

/**
 * This is used to retrieve the location data using GPS and used Network connection to increase the accuracy.
 */
public class LocationDataReader extends DataReader implements LocationListener {
    protected LocationManager locationManager;
    private Context mContext;
    private boolean canGetLocation = false;
    private static final String TAG = "Location Data";

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    public LocationDataReader(Context context) {
        mContext = context;
        getLocation();
    }

    public Location getLocation() {
        locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);

        // getting GPS status
        boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
        } else {
            this.canGetLocation = true;
            // First get location from Network Provider
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 0, 0, this);
                // MIN_TIME_BW_UPDATES,
                // MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                if (location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 0, 0, this);
                    //MIN_TIME_BW_UPDATES,
                    //MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d(TAG, "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }
        }
        return location;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(LocationDataReader.this);
        }
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        // return longitude
        return longitude;
    }

    @Override
    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void run() {
        Log.d(TAG, "running -Location");
        try {
            TimeUnit.MILLISECONDS.sleep(10000);
            double lat = getLatitude();
            double longit = getLongitude();
            if (lat != 0 && longit != 0) {
                SenseDataHolder.getLocationDataHolder().add(new LocationData(getLatitude(), getLongitude()));
            }
        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
            Log.e(TAG, " Location Data Retrieval Failed");
        }
    }


}
