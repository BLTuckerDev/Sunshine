package com.example.brett.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.brett.sunshine.data.WeatherContract;

import java.text.DateFormat;
import java.util.Date;

public final class ListViewItemFormatHelper {

	public boolean isMetric(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(context.getString(R.string.pref_units_key),
				context.getString(R.string.pref_units_default))
				.equals(context.getString(R.string.pref_units_default));
	}

	public String formatTemperature(double temperature, boolean isMetric) {
		double temp;
		if ( !isMetric ) {
			temp = 9*temperature/5+32;
		} else {
			temp = temperature;
		}
		return String.format("%.0f", temp);
	}

	public String formatDate(String dateString) {
		Date date = WeatherContract.getDateFromDb(dateString);
		return DateFormat.getDateInstance().format(date);
	}
}
