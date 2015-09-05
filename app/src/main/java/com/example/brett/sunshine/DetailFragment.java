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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.brett.sunshine.data.WeatherContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

	private static final int FORECAST_LOADER = 0;


	private String location;
	private String forecastDate;

	private ImageView imageView;

	private TextView dateTextView;
	private TextView descriptionTextView;

	private TextView highTextView;
	private TextView lowTextView;

	private TextView humidityTextView;
	private TextView windTextView;
	private TextView pressureTextView;

	private MyView compass;

	public DetailFragment(){
		this.setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
//		ShareActionProvider provider = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share_weather));
//		provider.setShareIntent(getShareIntent());
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

		imageView = (ImageView) rootView.findViewById(R.id.forecast_detail_icon);

		compass = (MyView) rootView.findViewById(R.id.forecast_detail_compass);

		dateTextView = (TextView) rootView.findViewById(R.id.forecast_detail_date);
		descriptionTextView = (TextView) rootView.findViewById(R.id.forecast_detail_description);

		highTextView = (TextView) rootView.findViewById(R.id.forecast_detail_high);
		lowTextView = (TextView) rootView.findViewById(R.id.forecast_detail_low);

		humidityTextView = (TextView) rootView.findViewById(R.id.forecast_detail_humidity);
		windTextView = (TextView) rootView.findViewById(R.id.forecast_detail_wind);
		pressureTextView = (TextView) rootView.findViewById(R.id.forecast_detail_pressure);


		Bundle args = getArguments();

		if(args != null && args.containsKey(DetailActivity.IntentExtras.ForecastDate)){
			forecastDate = args.getString(DetailActivity.IntentExtras.ForecastDate);
			getLoaderManager().initLoader(FORECAST_LOADER, null, this);
		}

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

			int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
			int resourceId = helper.getArtResourceForWeatherCondition(weatherId);
			imageView.setImageResource(resourceId);

			boolean isMetric = helper.isMetric(getActivity());

			dateTextView.setText(helper.formatDate(data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT))));

			String localizedDescription = WeatherIdStringConverter.getConverter().getStringForWeatherCondition(getActivity(), weatherId);
			descriptionTextView.setText(localizedDescription);

			highTextView.setText(helper.formatTemperature(getActivity(), data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)), isMetric ));
			lowTextView.setText(helper.formatTemperature(getActivity(),data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)), isMetric));
			humidityTextView.setText(getString(R.string.format_humidity, data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY))));

			float windSpeed = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
			float windDirection = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES));

			windTextView.setText(helper.getFormattedWind(getActivity(), windSpeed, windDirection));

			float pressure = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE));

			pressureTextView.setText(getString(R.string.format_pressure, pressure));

			compass.setWindSpeed(windSpeed);
			compass.setWindDirection(windDirection);

		}




	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}
}
