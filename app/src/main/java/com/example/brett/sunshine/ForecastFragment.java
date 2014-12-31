package com.example.brett.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

	private ArrayAdapter<String> listViewAdapter;

	public ForecastFragment() {
	}


	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
		inflater.inflate(R.menu.forecast_fragment, menu);
	}


	@Override
	public void onStart() {
		super.onStart();
		this.launchWeatherTask();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
			case R.id.action_refresh:
				this.launchWeatherTask();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}


	private void launchWeatherTask(){

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String zipcode = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

		FetchWeatherTask weatherTask = new FetchWeatherTask();
		weatherTask.execute(zipcode);
	}



	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.setHasOptionsMenu(true);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
		attachAdapter(listView);
		setupOnClickListener(listView);

		return rootView;
	}


	private void attachAdapter(ListView listview){
		List<String> forecastRepository = new ArrayList<>();
		listViewAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, forecastRepository);
		listview.setAdapter(listViewAdapter);
	}


	private void setupOnClickListener(final ListView listview){

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent explicitIntent = new Intent(getActivity(), DetailActivity.class);
				String weatherData = (String) listview.getAdapter().getItem(position);
				explicitIntent.putExtra(DetailActivity.IntentExtras.Forecast, weatherData);
				getActivity().startActivity(explicitIntent);
			}
		});

	}



	public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

		private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

		@Override
		protected String[] doInBackground(String... params) {

			if(params.length == 0){
				return null;
			}

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
						.appendQueryParameter("q", params[0])
						.appendQueryParameter("mode", "json")
						.appendQueryParameter("units", "metric")
						.appendQueryParameter("cnt", "7");



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
					return null;
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
					return null;
				}
				forecastJsonStr = buffer.toString();
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error ", e);
				// If the code didn't successfully get the weather data, there's no point in attempting
				// to parse it.
				return null;
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
				return getWeatherDataFromJson(forecastJsonStr, 7);
			} catch (JSONException e) {
				Log.e(LOG_TAG,  "Error ", e);
				return null;
			}
		}


		@Override
		protected void onPostExecute(String[] strings) {

			if(strings != null){
				listViewAdapter.clear();
				for(String forecast : strings){
					listViewAdapter.add(forecast);
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


			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

			String unitsPreference = prefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default));

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
		private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
				throws JSONException {

			// These are the names of the JSON objects that need to be extracted.
			final String OWM_LIST = "list";
			final String OWM_WEATHER = "weather";
			final String OWM_TEMPERATURE = "temp";
			final String OWM_MAX = "max";
			final String OWM_MIN = "min";
			final String OWM_DATETIME = "dt";
			final String OWM_DESCRIPTION = "main";

			JSONObject forecastJson = new JSONObject(forecastJsonStr);
			JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

			String[] resultStrs = new String[numDays];
			for(int i = 0; i < weatherArray.length(); i++) {
				// For now, using the format "Day, description, hi/low"
				String day;
				String description;
				String highAndLow;

				// Get the JSON object representing the day
				JSONObject dayForecast = weatherArray.getJSONObject(i);

				// The date/time is returned as a long.  We need to convert that
				// into something human-readable, since most people won't read "1400356800" as
				// "this saturday".
				long dateTime = dayForecast.getLong(OWM_DATETIME);
				day = getReadableDateString(dateTime);

				// description is in a child array called "weather", which is 1 element long.
				JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
				description = weatherObject.getString(OWM_DESCRIPTION);

				// Temperatures are in a child object called "temp".  Try not to name variables
				// "temp" when working with temperature.  It confuses everybody.
				JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
				double high = temperatureObject.getDouble(OWM_MAX);
				double low = temperatureObject.getDouble(OWM_MIN);

				highAndLow = formatHighLows(high, low);
				resultStrs[i] = day + " - " + description + " - " + highAndLow;
			}

			return resultStrs;
		}
	}
}
