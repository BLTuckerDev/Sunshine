package com.example.brett.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.brett.sunshine.sync.SunshineSyncAdapter;

public final class LocationStatusPreferenceManager {

    public int getCurrentLocationStatus(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.location_status_pref_key), SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN);
    }


    public void setCurrentStatusToUnknown(Context context){
        this.setCurrentLocationStatus(SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN, context);
    }


    public void setCurrentStatusToLocationInvalid(Context context) {
        this.setCurrentLocationStatus(SunshineSyncAdapter.LOCATION_STATUS_INVALID, context);
    }


    public void setCurrentLocationStatusToServerDown(Context context){
        this.setCurrentLocationStatus(SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN, context);
    }

    private void setCurrentLocationStatus(@SunshineSyncAdapter.LocationStatus int locationStatus, Context context){
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putInt(context.getString(R.string.location_status_pref_key), locationStatus);
        edit.apply();
    }
}
