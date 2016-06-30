package org.wso2.carbon.iot.android.sense.beacon;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
public class BeaconServiceUtility {

	private Context context;
	private PendingIntent pintent;
	private AlarmManager alarm;
	private Intent iService;

	public BeaconServiceUtility(Context context) {
		super();
		this.context = context;
		iService = new Intent(context, BeaconDetactorService.class);
		alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		pintent = PendingIntent.getService(context, 0, iService, 0);
	}

	public void onStart(BeaconManager iBeaconManager, BeaconConsumer consumer) {

		stopBackgroundScan();
		iBeaconManager.bind(consumer);

	}

	public void onStop(BeaconManager iBeaconManager, BeaconConsumer consumer) {

		iBeaconManager.unbind(consumer);
		startBackgroundScan();

	}

	private void stopBackgroundScan() {

		alarm.cancel(pintent);
		context.stopService(iService);
	}

	private void startBackgroundScan() {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 2);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 360000, pintent); // 6*60 * 1000
		context.startService(iService);
	}

}
