package com.example.brett.sunshine;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.example.brett.sunshine.data.WeatherContract;
import com.example.brett.sunshine.sync.SunshineSyncAdapter;

import junit.runner.Version;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity
		implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

	private boolean bindingPreference = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_general);
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
		bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
	}


	@Override
	protected void onResume() {

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(this);

        super.onResume();
	}


	@Override
	protected void onPause() {

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
	}


	/**
	 * Attaches a listener so the summary is always updated with the preference value.
	 * Also fires the listener once, to initialize the summary (so it shows up before the value
	 * is changed.)
	 */
	private void bindPreferenceSummaryToValue(Preference preference) {
		bindingPreference = true;
		// Set the listener to watch for value changes.
		preference.setOnPreferenceChangeListener(this);

		// Trigger the listener immediately with the preference's
		// current value.
		onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));

		bindingPreference = false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object value) {
        this.setPreferenceSummary(preference, value);
		return true;
	}


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Nullable
	@Override
	public Intent getParentActivityIntent() {
		return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals(getString(R.string.pref_location_key))){
            //if the location changed, then set status to unknown and immediately sync!
			new LocationStatusPreferenceManager().setCurrentStatusToUnknown(this);
            SunshineSyncAdapter.syncImmediately(this);
        } else if (key.equals(getString(R.string.pref_units_key))){
            //if the units changed we need to notify our data provider and tell him to update the weather entries accordingly.
            getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        } else if(key.equals(getString(R.string.location_status_pref_key))){
            //location status changed so update the preference to let the user know their current status.
            Preference locationPreference = findPreference(getString(R.string.pref_location_key));
            bindPreferenceSummaryToValue(locationPreference);
        }
	}


    private void setPreferenceSummary(Preference preference, Object value){

        String stringValue = value.toString();

        if ( !bindingPreference ) {
            if (preference.getKey().equals(getString(R.string.pref_location_key))) {
                SunshineSyncAdapter.initializeSyncAdapter(this);
                new LocationStatusPreferenceManager().setCurrentStatusToUnknown(this);
            } else {
                // notify code that weather may be impacted
                getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
            }
        }

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }

    }






}