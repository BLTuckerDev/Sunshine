package com.example.brett.sunshine;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.brett.sunshine.data.WeatherContract;


public class MainActivity extends ActionBarActivity implements ForecastFragmentCallbackListener {

	private final String LOG_TAG = MainActivity.class.getSimpleName();

	private boolean isTwoPaneMode = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(LOG_TAG, "onCreate()");

		if(findViewById(R.id.weather_detail_container) != null){
			isTwoPaneMode = true;
			if(savedInstanceState == null){
				getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, new DetailFragment()).commit();
			}
		}

		ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
		forecastFragment.setUseTodayLayout(!isTwoPaneMode);

	}


	@Override
	protected void onStart() {
		super.onStart();
		Log.d(LOG_TAG, "onStart()");
	}


	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume()");
	}


	@Override
	protected void onPause() {
		super.onPause();
		Log.d(LOG_TAG, "onPause()");
	}


	@Override
	protected void onStop() {
		super.onStop();
		Log.d(LOG_TAG, "onStop()");
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "onDestroy()");
	}


	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(LOG_TAG, "onRestart()");
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			startSettingsActivity();
			return true;
		}

		if(id == R.id.action_main_map_location){
			sendMapsIntent();
			return true;
		}


		return super.onOptionsItemSelected(item);
	}


	private void sendMapsIntent(){
		Intent mapsIntent = new Intent(Intent.ACTION_VIEW);
		Uri.Builder builder = new Uri.Builder();

		String location = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

		Uri geoUri = builder.scheme("geo")
				.authority("0,0")
				.appendQueryParameter("q", location)
				.build();

		mapsIntent.setData(geoUri);
		if(mapsIntent.resolveActivity(getPackageManager()) != null){
			startActivity(mapsIntent);
		}
	}


	private void startSettingsActivity(){
		Intent settingsIntent = new Intent(this, SettingsActivity.class);
		startActivity(settingsIntent);
	}


	@Override
	public void onItemSelected(String date) {

		if(isTwoPaneMode){
			Bundle args = new Bundle();
			args.putString(DetailActivity.IntentExtras.ForecastDate, date);
			DetailFragment df = new DetailFragment();
			df.setArguments(args);
			android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.weather_detail_container, df);
			ft.commit();
		} else {
			Intent explicitIntent = new Intent(this, DetailActivity.class);
			explicitIntent.putExtra(DetailActivity.IntentExtras.ForecastDate, date);
			startActivity(explicitIntent);
		}
	}
}
