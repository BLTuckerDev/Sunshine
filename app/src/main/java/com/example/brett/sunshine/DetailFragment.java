package com.example.brett.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

    private TextView humidityLabelView;
    private TextView windLabelView;
    private TextView pressureLabelView;


	private MyView compass;

	public DetailFragment(){
		this.setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if ( getActivity() instanceof DetailActivity ){
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.menu_detail, menu);
            finishCreatingMenu(menu);
        }
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        humidityLabelView = (TextView) rootView.findViewById(R.id.detail_humidity_label_textview);
        windLabelView = (TextView) rootView.findViewById(R.id.detail_wind_label_textview);
        pressureLabelView = (TextView) rootView.findViewById(R.id.detail_pressure_label_textview);

		Bundle args = getArguments();

		if(args != null && args.containsKey(DetailActivity.IntentExtras.ForecastDate)){
			forecastDate = args.getString(DetailActivity.IntentExtras.ForecastDate);
			getLoaderManager().initLoader(FORECAST_LOADER, null, this);
		}

		return rootView;
	}

    private void finishCreatingMenu(Menu menu){
        MenuItem menuItem = menu.findItem(R.id.action_share_weather);
        menuItem.setIntent(getShareIntent());
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
				sortOrder);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		populateWithWeatherData(data);
        startPostponedTransition();
	}


    private void populateWithWeatherData(Cursor data){

        if(data.moveToFirst()){

            WeatherFormatHelper helper = new WeatherFormatHelper();
            int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            String localizedDescription = WeatherResourceConverter.getConverter().getStringForWeatherCondition(getActivity(), weatherId);

            Glide.with(this)
                    .load(WeatherResourceConverter.getConverter().getArtUrlForWeatherCondition(getActivity(), weatherId))
                    .error(WeatherResourceConverter.getConverter().getArtResourceForWeatherCondition(weatherId))
                    .crossFade()
                    .into(imageView);

            imageView.setContentDescription(getString(R.string.a11y_forecast, localizedDescription));

            boolean isMetric = helper.isMetric(getActivity());

            dateTextView.setText(helper.getFullFriendlyDayString(getActivity(), data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT))));


            descriptionTextView.setText(localizedDescription);
            descriptionTextView.setContentDescription(getString(R.string.a11y_forecast, localizedDescription));

            String highTemp = helper.formatTemperature(getActivity(), data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)), isMetric);
            highTextView.setText(highTemp);
            highTextView.setContentDescription(getString(R.string.a11y_high_temp, highTemp));


            String lowTemp = helper.formatTemperature(getActivity(), data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)), isMetric);
            lowTextView.setText(lowTemp);
            lowTextView.setContentDescription(getString(R.string.a11y_low_temp, lowTemp));

            String humidity = getString(R.string.format_humidity, data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY)));
            humidityTextView.setText(humidity);
            humidityTextView.setContentDescription(humidity);
            humidityLabelView.setContentDescription(humidity);

            float windSpeed = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
            float windDirection = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES));

            String wind = helper.getFormattedWind(getActivity(), windSpeed, windDirection);
            windTextView.setText(wind);
            windTextView.setContentDescription(wind);
            windLabelView.setContentDescription(wind);

            float pressure = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE));

            String pressureString = getString(R.string.format_pressure, pressure);
            pressureTextView.setText(pressureString);
            pressureTextView.setContentDescription(pressureString);
            pressureLabelView.setContentDescription(pressureString);

            compass.setWindSpeed(windSpeed);
            compass.setWindDirection(windDirection);

        }


    }


    private void startPostponedTransition(){

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar) getView().findViewById(R.id.toolbar);

        if(activity instanceof DetailActivity){

            activity.supportStartPostponedEnterTransition();

            if(null != toolbar){

                activity.setSupportActionBar(toolbar);

                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

        } else {

            if(null != toolbar){
                Menu menu = toolbar.getMenu();

                if ( null != menu ) {
                    menu.clear();
                }

                toolbar.inflateMenu(R.menu.menu_detail);
                finishCreatingMenu(menu);
            }

        }



    }

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}


    private boolean isUsingLocalGraphics() {
        Context context = getActivity();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String sunshineArtPack = context.getString(R.string.pref_art_pack_sunshine);
        return prefs.getString(context.getString(R.string.pref_art_pack_key), sunshineArtPack).equals(sunshineArtPack);
    }

}
