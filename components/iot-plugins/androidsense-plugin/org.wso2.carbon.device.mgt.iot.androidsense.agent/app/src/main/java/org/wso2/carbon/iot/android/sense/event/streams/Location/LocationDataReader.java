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
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.wso2.carbon.iot.android.sense.event.streams.DataReader;
import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * This is used to retrieve the location data using GPS and used Network connection to increase the accuracy.
 */
public class LocationDataReader extends DataReader implements LocationListener {
    protected LocationManager locationManager;
    private final Context mContext;

    LocationData gps;

    static final Double EARTH_RADIUS = 6371.00;

    // flag for GPS status
    private boolean isGPSEnabled = false;

    // flag for network status
    private boolean isNetworkEnabled = false;

    // flag for GPS status
    private boolean canGetLocation = false;
    //private boolean canGetLocation = false;
    private static final String TAG = LocationDataReader.class.getName();

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    double lat_old=0.0;
    double lon_old=0.0;
    double time;
    float speed = 0.0f;
    private long lastUpdate;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    public LocationDataReader(Context context) {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try {

            locationManager = (LocationManager) mContext
                    .getSystemService(mContext.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
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
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
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

        } catch (Exception e) {
            Log.e(TAG, "Failed to capture location data.");
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
        Log.v("Debug", "in onLocation changed..");
        if(location!=null){
            long curTime = System.currentTimeMillis();

            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;
            Calendar c=Calendar.getInstance();
            c.setTimeInMillis(diffTime);

            time=c.get(Calendar.HOUR);

            locationManager.removeUpdates(LocationDataReader.this);
            //String Speed = "Device Speed: " +location.getSpeed();
            latitude=location.getLongitude();
            longitude =location.getLatitude();

            double distance =CalculationByDistance(latitude, longitude, lat_old, lon_old)/1000;

            speed = (float)distance/(float)time;
            Toast.makeText(mContext, longitude+"\n"+latitude+"\nDistance is: "
                    +distance+"\nSpeed is: "+speed , Toast.LENGTH_SHORT).show();


            Intent intent = new Intent("speedUpdate");
            intent.putExtra("speed", speed);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            lat_old=latitude;
            lon_old=longitude;
        }

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
                Log.d(TAG, "YYY " + getLatitude() + ", XXX " +  getLongitude());
                gps = new LocationData(getLatitude(), getLongitude());
                SenseDataHolder.getLocationDataHolder().add(gps);

            }
        } catch (InterruptedException e) {
            // Restore the interrupted status
            Thread.currentThread().interrupt();
            Log.e(TAG, " Location Data Retrieval Failed");
        }
    }

    public double CalculationByDistance(double lat1, double lon1, double lat2, double lon2) {
        double Radius = EARTH_RADIUS;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Radius * c;
    }



}
