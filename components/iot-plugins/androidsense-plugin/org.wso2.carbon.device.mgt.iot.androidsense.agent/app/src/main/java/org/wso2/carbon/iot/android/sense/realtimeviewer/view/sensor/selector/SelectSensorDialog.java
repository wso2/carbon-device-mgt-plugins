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
package org.wso2.carbon.iot.android.sense.realtimeviewer.view.sensor.selector;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.wso2.carbon.iot.android.sense.realtimeviewer.sensorlisting.SupportedSensors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Functionality
 * <p/>
 * Show the list of available sensors in a list
 * Get the user selections
 * Put them in to shared preferences
 */

public class SelectSensorDialog extends DialogFragment {

    protected boolean[] selections = new boolean[SupportedSensors.SUPPORTED_SENSOR_COUNT];
    Activity activity;
    SensorListListener sensorListListener;
    private Set<String> selectedSensorSet = new HashSet<>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Sensors");
        activity = getActivity();

        SharedPreferences preferences = getActivity().getSharedPreferences(SupportedSensors.AVAILABLE_SENSORS, Context.
                MODE_MULTI_PROCESS);

        Set<String> set = preferences.getStringSet(SupportedSensors.GET_AVAILABLE_SENSORS, null);
        final CharSequence[] sequence = getSequence(set);

        final boolean[] pos = new boolean[selections.length];
        final boolean[] neg = new boolean[selections.length];

        builder.setMultiChoiceItems(sequence, selections, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    selectedSensorSet.add(sequence[which].toString());

                    pos[which] = true;
                } else {
                    selectedSensorSet.remove(sequence[which].toString());
                    neg[which] = true;
                }
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("Click", "Ok");
                //call sensorDataMap reading class
                sensorListListener.onDialogPositiveClick(SelectSensorDialog.this);

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("Click", "Cancel");
                for (int i = 0; i < SupportedSensors.SUPPORTED_SENSOR_COUNT; i++) {

                    if (pos[i])
                        selections[i] = false;
                    if (neg[i])
                        selections[i] = true;
                }
            }
        });

        return builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            sensorListListener = (SensorListListener) getActivity();
        } catch (ClassCastException ex) {
            throw new ClassCastException(activity.toString() + " must implement the SensorListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * Interface to be implemented by the parent
     */
    public CharSequence[] getSequence(Set<String> sensorset) {
        CharSequence[] seq;
        String[] seq2 = sensorset.toArray(new String[sensorset.size()]);
        seq = Arrays.copyOf(seq2, seq2.length);
        return seq;
    }

    public Set<String> getSet() {
        return this.selectedSensorSet;
    }

    public interface SensorListListener {
        void onDialogPositiveClick(SelectSensorDialog dialog);
    }

}
