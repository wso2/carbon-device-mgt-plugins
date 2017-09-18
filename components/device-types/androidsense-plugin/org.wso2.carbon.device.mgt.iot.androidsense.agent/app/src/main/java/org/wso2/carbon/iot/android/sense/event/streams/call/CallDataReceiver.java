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

package org.wso2.carbon.iot.android.sense.event.streams.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;

public class CallDataReceiver extends BroadcastReceiver {
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static long startTime;
    private static boolean isIncoming;
    private static String lastNotifiedNumber;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            lastNotifiedNumber = intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER);
        } else {
            String extraState = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            int state = 0;
            if (extraState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                state = TelephonyManager.CALL_STATE_IDLE;
            } else if (extraState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if (extraState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;
            }

            if (lastState == state) {
                return; // Nothing has been changed
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    // Receiving a call
                    isIncoming = true;
                    startTime = System.currentTimeMillis();
                    // If incoming call, get the incoming number
                    lastNotifiedNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        // RINGING -> OFFHOOK = ANSWERED
                        isIncoming = true;
                        startTime = System.currentTimeMillis();
                    } else {
                        // NOT RINGING -> OFFHOOK = OUTGOING
                        isIncoming = false;
                        startTime = System.currentTimeMillis();
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        // RINGING -> IDLE = MISSED
                        SenseDataHolder.getCallDataHolder().add(new CallData(CallData.Type.MISSED, lastNotifiedNumber, startTime, System.currentTimeMillis()));
                    } else if (isIncoming) {
                        // Incoming (OFFHOOK) -> IDLE = INCOMING CALL ENDED
                        SenseDataHolder.getCallDataHolder().add(new CallData(CallData.Type.INCOMING, lastNotifiedNumber, startTime, System.currentTimeMillis()));
                    } else {
                        // Not Incoming -> IDLE = OUTGOING CALL ENDED
                        SenseDataHolder.getCallDataHolder().add(new CallData(CallData.Type.OUTGOING, lastNotifiedNumber, startTime, System.currentTimeMillis()));
                    }
                    break;
            }
            lastState = state;
        }
    }
}
