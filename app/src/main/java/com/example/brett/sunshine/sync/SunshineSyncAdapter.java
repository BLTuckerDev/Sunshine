package com.example.brett.sunshine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.brett.sunshine.ListViewItemFormatHelper;
import com.example.brett.sunshine.MainActivity;
import com.example.brett.sunshine.NotificationPreferenceFetcher;
import com.example.brett.sunshine.PreferredLocationFetcher;
import com.example.brett.sunshine.R;
import com.example.brett.sunshine.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public final class SunshineSyncAdapter extends AbstractThreadedSyncAdapter{

	private final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();

	public static final int SYNC_INTERVAL = 60 * 180;
	public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

	private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
			WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
			WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
			WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
			WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
	};

	// these indices must match the projection
	private static final int INDEX_WEATHER_ID = 0;
	private static final int INDEX_MAX_TEMP = 1;
	private static final int INDEX_MIN_TEMP = 2;
	private static final int INDEX_SHORT_DESC = 3;

	private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
	private static final int WEATHER_NOTIFICATION_ID = 3004;

	public SunshineSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}


	@Override
	public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

		String locationQuery = new PreferredLocationFetcher().getPreferredLocation(getContext());
		final int numberOfDays = 14;

		HttpURLConnection urlConnection = null;
		BufferedReader reader = null;

		// Will contain the raw JSON response as a string.
		String forecastJsonStr = null;

		try {
			Uri.Builder uriBuilder = new Uri.Builder();
			uriBuilder.scheme("http")
					.authority("api.openweathermap.org")
					.appendPath("data")
					.appendPath("2.5")
					.appendPath("forecast")
					.appendPath("daily")
					.appendQueryParameter("q",locationQuery)
					.appendQueryParameter("mode", "json")
					.appendQueryParameter("units", "metric")
					.appendQueryParameter("cnt", String.valueOf(numberOfDays));

			// Construct the URL for the OpenWeatherMap query
			// Possible parameters are available at OWM's forecast API page, at
			// http://openweathermap.org/API#forecast
			URL url = new URL(uriBuilder.build().toString());

			// Create the request to OpenWeatherMap, and open the connection
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

			// Read the input stream into a String
			InputStream inputStream = urlConnection.getInputStream();
			StringBuffer buffer = new StringBuffer();
			if (inputStream == null) {
				// Nothing to do.
				return;
			}
			reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = reader.readLine()) != null) {
				// Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
				// But it does make debugging a *lot* easier if you print out the completed
				// buffer for debugging.
				buffer.append(line + "\n");
			}

			if (buffer.length() == 0) {
				// Stream was empty.  No point in parsing.
				return;
			}
			forecastJsonStr = buffer.toString();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error ", e);
			// If the code didn't successfully get the weather data, there's no point in attempting
			// to parse it.
			return;
		} finally{
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (final IOException e) {
					Log.e(LOG_TAG, "Error closing stream", e);
				}
			}
		}

		Log.d(LOG_TAG, forecastJsonStr);

		try {
			getWeatherDataFromJson(forecastJsonStr, numberOfDays, locationQuery);
		} catch (JSONException e) {
			Log.e(LOG_TAG,  "Error ", e);
		}

	}


	private long addLocation(String locationSetting, String cityName, double latitude, double longitude){
		Cursor cursor = getContext().getContentResolver().query(
				WeatherContract.LocationEntry.CONTENT_URI,
				new String[]{WeatherContract.LocationEntry._ID},
				WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
				new String[]{locationSetting},
				null);

		if (cursor.moveToFirst()) {
			Log.v(LOG_TAG, "Found it in the database!");
			int locationIdIndex = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
			return cursor.getLong(locationIdIndex);
		} else {
			Log.v(LOG_TAG, "Didn't find it in the database, inserting now!");
			ContentValues locationValues = new ContentValues();
			locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
			locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
			locationValues.put(WeatherContract.LocationEntry.COLUMN_LATITUDE, latitude);
			locationValues.put(WeatherContract.LocationEntry.COLUMN_LONGITUDE, longitude);

			Uri locationInsertUri = getContext().getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, locationValues);

			return ContentUris.parseId(locationInsertUri);
		}
	}


	private void notifyWeather() {

		Context context = getContext();

		NotificationPreferenceFetcher notificationPrefFetcher = new NotificationPreferenceFetcher();

		if(!notificationPrefFetcher.areNotificationsEnabled(context)){
			return;
		}

		//checking the last update and notify if it' the first of the day
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String lastNotificationKey = context.getString(R.string.pref_last_notification);
		long lastSync = prefs.getLong(lastNotificationKey, 0);

		if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
			// Last sync was more than 1 day ago, let's send a notification with the weather.
			String locationQuery = new PreferredLocationFetcher().getPreferredLocation(context);

			Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, WeatherContract.getDbDateString(new Date()));

			// we'll query our contentProvider, as always
			Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

			if (cursor.moveToFirst()) {
				int weatherId = cursor.getInt(INDEX_WEATHER_ID);
				double high = cursor.getDouble(INDEX_MAX_TEMP);
				double low = cursor.getDouble(INDEX_MIN_TEMP);
				String desc = cursor.getString(INDEX_SHORT_DESC);

				ListViewItemFormatHelper formatHelper = new ListViewItemFormatHelper();

				int iconId = formatHelper.getIconResourceForWeatherCondition(weatherId);
				String title = context.getString(R.string.app_name);

				boolean isMetric = formatHelper.isMetric(getContext());
				// Define the text of the forecast.
				String contentText = String.format(context.getString(R.string.format_notification),
						desc,
						formatHelper.formatTemperature(context, high,isMetric),
						formatHelper.formatTemperature(context, low, isMetric));

				//build your notification here.

				NotificationCompat.Builder notifcationBuilder = new NotificationCompat.Builder(getContext());

				notifcationBuilder.setSmallIcon(iconId)
						.setContentTitle(title)
						.setContentText(contentText);

				Intent resultIntent = new Intent(getContext(), MainActivity.class);

				TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
				stackBuilder.addParentStack(MainActivity.class);
				stackBuilder.addNextIntent(resultIntent);

				PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

				notifcationBuilder.setContentIntent(resultPendingIntent);

				NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

				notificationManager.notify(WEATHER_NOTIFICATION_ID, notifcationBuilder.build());


				//refreshing last sync
				SharedPreferences.Editor editor = prefs.edit();
				editor.putLong(lastNotificationKey, System.currentTimeMillis());
				editor.commit();
			}
		}

	}


	/* The date/time conversion code is going to be moved outside the asynctask later,
	 * so for convenience we're breaking it out into its own method now.
	 */
	private String getReadableDateString(long time){
		// Because the API returns a unix timestamp (measured in seconds),
		// it must be converted to milliseconds in order to be converted to valid date.
		Date date = new Date(time * 1000);
		SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
		return format.format(date).toString();
	}

	/**
	 * Prepare the weather high/lows for presentation.
	 */
	private String formatHighLows(double high, double low) {
		// For presentation, assume the user doesn't care about tenths of a degree.
		long roundedHigh = Math.round(high);
		long roundedLow = Math.round(low);


		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

		String unitsPreference = prefs.getString(getContext().getString(R.string.pref_units_key), getContext().getString(R.string.pref_units_default));

		if(unitsPreference.equals("imperial")){
			roundedHigh = Math.round(roundedHigh * 1.8 + 32);
			roundedLow = Math.round(roundedLow * 1.8 + 32);
		}




		String highLowStr = roundedHigh + "/" + roundedLow;
		return highLowStr;
	}

	/**
	 * Take the String representing the complete forecast in JSON Format and
	 * pull out the data we need to construct the Strings needed for the wireframes.
	 *
	 * Fortunately parsing is easy:  constructor takes the JSON string and converts it
	 * into an Object hierarchy for us.
	 */
	private void getWeatherDataFromJson(String forecastJsonStr, int numDays, String locationQuery)
			throws JSONException {

		// These are the names of the JSON objects that need to be extracted.

		// Location information
		final String OWM_CITY = "city";
		final String OWM_CITY_NAME = "name";
		final String OWM_COORD = "coord";
		final String OWM_COORD_LAT = "lat";
		final String OWM_COORD_LONG = "lon";

		// Weather information.  Each day's forecast info is an element of the "list" array.
		final String OWM_LIST = "list";

		final String OWM_DATETIME = "dt";
		final String OWM_PRESSURE = "pressure";
		final String OWM_HUMIDITY = "humidity";
		final String OWM_WINDSPEED = "speed";
		final String OWM_WIND_DIRECTION = "deg";

		// All temperatures are children of the "temp" object.
		final String OWM_TEMPERATURE = "temp";
		final String OWM_MAX = "max";
		final String OWM_MIN = "min";

		final String OWM_WEATHER = "weather";
		final String OWM_DESCRIPTION = "main";
		final String OWM_WEATHER_ID = "id";

		JSONObject forecastJson = new JSONObject(forecastJsonStr);
		JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

		JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
		String cityName = cityJson.getString(OWM_CITY_NAME);
		JSONObject coordJSON = cityJson.getJSONObject(OWM_COORD);
		double cityLatitude = coordJSON.getLong(OWM_COORD_LAT);
		double cityLongitude = coordJSON.getLong(OWM_COORD_LONG);

		Log.v(LOG_TAG, cityName + ", with coord: " + cityLatitude + " " + cityLongitude);

		// Insert the location into the database.
		// The function referenced here is not yet implemented, so we've commented it out for now.
		long locationID = addLocation(locationQuery, cityName, cityLatitude, cityLongitude);

		// Get and insert the new weather information into the database
		Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

		for(int i = 0; i < weatherArray.length(); i++) {
			// These are the values that will be collected.

			long dateTime;
			double pressure;
			int humidity;
			double windSpeed;
			double windDirection;

			double high;
			double low;

			String description;
			int weatherId;

			// Get the JSON object representing the day
			JSONObject dayForecast = weatherArray.getJSONObject(i);

			// The date/time is returned as a long.  We need to convert that
			// into something human-readable, since most people won't read "1400356800" as
			// "this saturday".
			dateTime = dayForecast.getLong(OWM_DATETIME);

			pressure = dayForecast.getDouble(OWM_PRESSURE);
			humidity = dayForecast.getInt(OWM_HUMIDITY);
			windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
			windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

			// Description is in a child array called "weather", which is 1 element long.
			// That element also contains a weather code.
			JSONObject weatherObject =
					dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
			description = weatherObject.getString(OWM_DESCRIPTION);
			weatherId = weatherObject.getInt(OWM_WEATHER_ID);

			// Temperatures are in a child object called "temp".  Try not to name variables
			// "temp" when working with temperature.  It confuses everybody.
			JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
			high = temperatureObject.getDouble(OWM_MAX);
			low = temperatureObject.getDouble(OWM_MIN);

			ContentValues weatherValues = new ContentValues();

			weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationID);
			weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
			weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
			weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
			weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
			weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
			weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
			weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
			weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
			weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

			cVVector.add(weatherValues);

			if(cVVector.size() > 0){
				ContentValues[] valuesArray = new ContentValues[cVVector.size()];
				cVVector.copyInto(valuesArray);

				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, -1);
				Date yesterday = cal.getTime();
				String queryParam = WeatherContract.getDbDateString(yesterday);
				 int deletedRows = getContext().getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
						  WeatherContract.WeatherEntry.COLUMN_DATETEXT + " <= ?",
						  new String[]{queryParam});

				Log.d(LOG_TAG, "Deleted: " + deletedRows + " + rows of old weather data");

				getContext().getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, valuesArray);
				this.notifyWeather();
			}
		}
	}


	public static void syncImmediately(Context context) {
		Bundle bundle = new Bundle();
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
		bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(getSyncAccount(context),
				context.getString(R.string.content_authority), bundle);
	}



	public static Account getSyncAccount(Context context) {
		// Get an instance of the Android account manager
		AccountManager accountManager =
				(AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

		// Create the account type and default account
		Account newAccount = new Account(
				context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

		// If the password doesn't exist, the account doesn't exist
		if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
			if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
				return null;
			}
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

			onAccountCreated(newAccount, context);


		}
		return newAccount;
	}


	public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
		Account account = getSyncAccount(context);
		String authority = context.getString(R.string.content_authority);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// we can enable inexact timers in our periodic sync
			SyncRequest request = new SyncRequest.Builder().
					setExtras(new Bundle()).
					syncPeriodic(syncInterval, flexTime).
					setSyncAdapter(account, authority).build();
			ContentResolver.requestSync(request);
		} else {
			ContentResolver.addPeriodicSync(account,
					authority, new Bundle(), syncInterval);
		}
	}

	private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
		SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
		ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
		syncImmediately(context);
	}

	public static void initializeSyncAdapter(Context context) {
		getSyncAccount(context);
	}
}
