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
package org.wso2.carbon.iot.android.sense.beacon;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import agent.sense.android.iot.carbon.wso2.org.wso2_senseagent.R;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

public class BeaconDetactorService extends Service implements BeaconConsumer {

	private BeaconManager iBeaconManager = BeaconManager.getInstanceForApplication(this);

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		new Thread(){
			@Override
			public void run() {
				iBeaconManager.bind(BeaconDetactorService.this);
			}
		}.start();


		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();


		final Handler handler = new Handler();
		final Runnable runnable = new Runnable() {

			@Override
			public void run() {
				//stopSelf();
			}
		};
		handler.postDelayed(runnable, 10000);
	}

	@Override
	public void onDestroy() {
		iBeaconManager.unbind(this);
		super.onDestroy();
	}

	@Override
	public void onBeaconServiceConnect() {
		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
			@Override
			public void didEnterRegion(Region region) {
				Log.e("BeaconDetactorService", "didEnterRegion");
				generateNotification(BeaconDetactorService.this, region.getUniqueId()
						+ ": just saw this iBeacon for the first time");
			}

			@Override
			public void didExitRegion(Region region) {
				Log.e("BeaconDetactorService", "didExitRegion");
				generateNotification(BeaconDetactorService.this, region.getUniqueId() + ": is no longer visible");
			}

			@Override
			public void didDetermineStateForRegion(int state, Region region) {
				Log.e("BeaconDetactorService", "didDetermineStateForRegion:" + state);
			}

		});

		try {
			iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	private static void generateNotification(Context context, String message) {

		Intent launchIntent = new Intent(context, MonitoringActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(
				0,
				new NotificationCompat.Builder(context).setWhen(System.currentTimeMillis())
						.setSmallIcon(R.drawable.beacon).setTicker(message)
						.setContentTitle(context.getString(R.string.app_name)).setContentText(message)
						.setContentIntent(PendingIntent.getActivity(context, 0, launchIntent, 0)).setAutoCancel(true)
						.build());

	}

}