package com.example.brett.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new ForecastFragment())
					.commit();
		}
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
}
