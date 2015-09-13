package com.example.brett.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


public class DetailActivity extends AppCompatActivity  {

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

}
