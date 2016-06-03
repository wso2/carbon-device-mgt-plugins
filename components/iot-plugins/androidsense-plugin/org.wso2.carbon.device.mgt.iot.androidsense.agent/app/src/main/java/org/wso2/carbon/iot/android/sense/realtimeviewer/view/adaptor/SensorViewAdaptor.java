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
package org.wso2.carbon.iot.android.sense.realtimeviewer.view.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.wso2.carbon.iot.android.sense.realtimeviewer.event.realtimesensor.RealTimeSensor;
import java.util.List;
import agent.sense.android.iot.carbon.wso2.org.wso2_senseagent.R;

/**
 * Adaptor for populate the ListView.
 * Takes list of Sensor readings
 */
public class SensorViewAdaptor extends BaseAdapter {

    private Context context;
    private List<RealTimeSensor> data;

    public SensorViewAdaptor(Context context, List<RealTimeSensor> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;

        if (convertView == null) {
            view = inflater.inflate(R.layout.display_sensor_values, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.valuesX = (TextView) view.findViewById(R.id.X);
            holder.valuesY = (TextView) view.findViewById(R.id.Y);
            holder.valuesZ = (TextView) view.findViewById(R.id.Z);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        RealTimeSensor data = this.data.get(position);

        holder.name.setText(data.getName());
        holder.valuesX.setText(data.getValueX());
        holder.valuesY.setText(data.getValueY());
        holder.valuesZ.setText(data.getValueZ());

        return view;

    }

    private class ViewHolder {
        public TextView name;
        public TextView valuesX;
        public TextView valuesY;
        public TextView valuesZ;
    }
}
