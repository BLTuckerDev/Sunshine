package com.example.brett.sunshine;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class DetailActivity extends ActionBarActivity {

	public static class IntentExtras {
		public static final String ForecastDate = "forecastDate";
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		if (savedInstanceState == null) {

			DetailFragment df = new DetailFragment();
			Bundle args = new Bundle();
			args.putString(DetailActivity.IntentExtras.ForecastDate, getIntent().getExtras().getString(IntentExtras.ForecastDate));
			df.setArguments(args);

			getSupportFragmentManager().beginTransaction()
					.add(R.id.weather_detail_container,df)
					.commit();
		}

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


}
