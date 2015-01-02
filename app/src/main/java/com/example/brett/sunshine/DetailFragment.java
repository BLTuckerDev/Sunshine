package com.example.brett.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.brett.sunshine.data.WeatherContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

	private static final int FORECAST_LOADER = 0;


	private String location;
	private String forecastDate;

	private TextView dateTextView;
	private TextView descriptionTextView;

	private TextView highTextView;
	private TextView lowTextView;

	private TextView humidityTextView;
	private TextView windTextView;
	private TextView pressureTextView;

	public DetailFragment(){
		this.setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share_weather));
		provider.setShareIntent(getShareIntent());
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}


	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {

		Bundle extras = getActivity().getIntent().getExtras();

		if(extras != null){
			forecastDate = extras.getString(DetailActivity.IntentExtras.ForecastDate);
			getLoaderManager().initLoader(FORECAST_LOADER, null, this);

		}

		super.onActivityCreated(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

		dateTextView = (TextView) rootView.findViewById(R.id.detail_date);
		descriptionTextView = (TextView) rootView.findViewById(R.id.detail_description);

		highTextView = (TextView) rootView.findViewById(R.id.detail_high);
		lowTextView = (TextView) rootView.findViewById(R.id.detail_low);

		humidityTextView = (TextView) rootView.findViewById(R.id.detail_humidity);
		windTextView = (TextView) rootView.findViewById(R.id.detail_wind);
		pressureTextView = (TextView) rootView.findViewById(R.id.detail_pressure);

		return rootView;
	}


	private Intent getShareIntent(){
		Intent shareIntent = new Intent(Intent.ACTION_SEND);

		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Weather Forecast");
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		shareIntent.putExtra(Intent.EXTRA_TEXT, forecastDate + "#sunshine");

		return shareIntent;
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

		location = new PreferredLocationFetcher().getPreferredLocation(getActivity());
		Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
				location, forecastDate);

		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(
				getActivity(),
				weatherForLocationUri,
				null, //FORECAST_COLUMNS,
				null,
				null,
				sortOrder
		);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		if(data.moveToFirst()){

			ListViewItemFormatHelper helper = new ListViewItemFormatHelper();
			boolean isMetric = helper.isMetric(getActivity());

			dateTextView.setText(helper.formatDate(data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT))));
			descriptionTextView.setText(data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC)));
			highTextView.setText(helper.formatTemperature(data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)), isMetric ));
			lowTextView.setText(helper.formatTemperature(data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)), isMetric));
			humidityTextView.setText(data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY)));
			windTextView.setText(data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED)));
			pressureTextView.setText(data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE)));

		}




	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}
}
