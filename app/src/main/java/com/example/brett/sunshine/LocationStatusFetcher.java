package com.example.brett.sunshine;

import android.content.Context;
import android.preference.PreferenceManager;

import com.example.brett.sunshine.sync.SunshineSyncAdapter;

public final class LocationStatusFetcher {

    public int getCurrentLocationStatus(Context context){

        return PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.location_status_pref_key), SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN);

    }

}
