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

import android.app.Activity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

import agent.sense.android.iot.carbon.wso2.org.wso2_senseagent.R;
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconType;
import uk.co.alt236.bluetoothlelib.device.beacon.BeaconUtils;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;
import uk.co.alt236.easycursor.objectcursor.EasyObjectCursor;

public class LeDeviceListAdapter extends SimpleCursorAdapter {
    private final LayoutInflater mInflator;
    private final Activity mActivity;

    public static final DecimalFormat DOUBLE_TWO_DIGIT_ACCURACY = new DecimalFormat("#.##");
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public LeDeviceListAdapter(final Activity activity, final EasyObjectCursor<BluetoothLeDevice> cursor) {
        super(activity, R.layout.list_item_device, cursor, new String[0], new int[0], 0);
        mInflator = activity.getLayoutInflater();
        mActivity = activity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EasyObjectCursor<BluetoothLeDevice> getCursor() {
        return ((EasyObjectCursor<BluetoothLeDevice>) super.getCursor());
    }

    @Override
    public BluetoothLeDevice getItem(final int i) {
        return getCursor().getItem(i);
    }

    @Override
    public long getItemId(final int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.list_item_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.deviceRssi = (TextView) view.findViewById(R.id.device_rssi);
            viewHolder.deviceIcon = (ImageView) view.findViewById(R.id.device_icon);
            viewHolder.deviceLastUpdated = (TextView) view.findViewById(R.id.device_last_update);
            viewHolder.ibeaconMajor = (TextView) view.findViewById(R.id.ibeacon_major);
            viewHolder.ibeaconMinor = (TextView) view.findViewById(R.id.ibeacon_minor);
            viewHolder.ibeaconDistance = (TextView) view.findViewById(R.id.ibeacon_distance);
            viewHolder.ibeaconUUID = (TextView) view.findViewById(R.id.ibeacon_uuid);
            viewHolder.ibeaconTxPower = (TextView) view.findViewById(R.id.ibeacon_tx_power);
            viewHolder.ibeaconSection = view.findViewById(R.id.ibeacon_section);
            viewHolder.ibeaconDistanceDescriptor = (TextView) view.findViewById(R.id.ibeacon_distance_descriptor);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final BluetoothLeDevice device = getCursor().getItem(i);
        final String deviceName = device.getName();

        final double rssi = device.getRssi();

        if (deviceName != null && deviceName.length() > 0) {
            viewHolder.deviceName.setText(deviceName + " (" + BeaconMonitoringActivity.itemMap.get(device.getAddress())  + ")");
        } else {
            viewHolder.deviceName.setText(R.string.unknown_device);
        }


        if (BeaconUtils.getBeaconType(device) == BeaconType.IBEACON) {
            final IBeaconDevice iBeacon = new IBeaconDevice(device);
            final String accuracy = DOUBLE_TWO_DIGIT_ACCURACY.format(iBeacon.getAccuracy());

            viewHolder.deviceIcon.setImageResource(R.drawable.beacon);
            viewHolder.ibeaconSection.setVisibility(View.VISIBLE);
            viewHolder.ibeaconMajor.setText(String.valueOf(iBeacon.getMajor()));
            viewHolder.ibeaconMinor.setText(String.valueOf(iBeacon.getMinor()));
            viewHolder.ibeaconTxPower.setText(String.valueOf(iBeacon.getCalibratedTxPower()));
            viewHolder.ibeaconUUID.setText(iBeacon.getUUID());
            viewHolder.ibeaconDistance.setText(
                    mActivity.getString(R.string.formatter_meters, accuracy));
            viewHolder.ibeaconDistanceDescriptor.setText(iBeacon.getDistanceDescriptor().toString());
        } else {
            if(device.getRssi() > BeaconMonitoringActivity.MINIMUM_DISTANCE){
                viewHolder.deviceIcon.setImageResource(R.drawable.beacon_red);
            }else{
                viewHolder.deviceIcon.setImageResource(R.drawable.beacon);
            }

            viewHolder.ibeaconSection.setVisibility(View.GONE);
        }


        final String rssiString =
                mActivity.getString(R.string.formatter_db, String.valueOf(rssi));
        final String runningAverageRssiString =
                mActivity.getString(R.string.formatter_db, String.valueOf(device.getRunningAverageRssi()));

        viewHolder.deviceLastUpdated.setText(
                android.text.format.DateFormat.format(
                       TIME_FORMAT, new java.util.Date(device.getTimestamp())));
        viewHolder.deviceAddress.setText(device.getAddress());
        viewHolder.deviceRssi.setText(rssiString + " / " + runningAverageRssiString);
        return view;
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
        TextView ibeaconUUID;
        TextView ibeaconMajor;
        TextView ibeaconMinor;
        TextView ibeaconTxPower;
        TextView ibeaconDistance;
        TextView ibeaconDistanceDescriptor;
        TextView deviceLastUpdated;
        View ibeaconSection;
        ImageView deviceIcon;
    }

}