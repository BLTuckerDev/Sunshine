package com.example.brett.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class NotificationPreferenceFetcher {

	public boolean areNotificationsEnabled(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(context.getString(R.string.notification_preference_key), true);
	}
}
