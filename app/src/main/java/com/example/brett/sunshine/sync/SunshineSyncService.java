package com.example.brett.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public final class SunshineSyncService extends Service {

	private static final Object syncAdapterLock = new Object();
	private static SunshineSyncAdapter syncAdapter = null;


	@Override
	public void onCreate() {

		synchronized (syncAdapterLock){
			if(syncAdapter == null){
				syncAdapter = new SunshineSyncAdapter(getApplicationContext(), true);
			}
		}


	}


	@Override
	public IBinder onBind(Intent intent) {
		return syncAdapter.getSyncAdapterBinder();
	}
}
