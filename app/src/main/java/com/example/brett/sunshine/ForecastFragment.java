package com.example.brett.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.brett.sunshine.data.WeatherContract;

import java.util.Date;

import static com.example.brett.sunshine.data.WeatherContract.WeatherEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

	private static final int FORECAST_LOADER = 0;
	private String mLocation;

	private SimpleCursorAdapter listViewAdapter;

	private static final String[] FORECAST_COLUMNS = {
			// In this case the id needs to be fully qualified with a table name, since
			// the content provider joins the location & weather tables in the background
			// (both have an _id column)
			// On the one hand, that's annoying.  On the other, you can search the weather table
			// using the location set by the user, which is only in the Location table.
			// So the convenience is worth it.
			WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
			WeatherEntry.COLUMN_DATETEXT,
			WeatherEntry.COLUMN_SHORT_DESC,
			WeatherEntry.COLUMN_MAX_TEMP,
			WeatherEntry.COLUMN_MIN_TEMP,
			WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
	};

	public ForecastFragment() {
	}


	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater){
		inflater.inflate(R.menu.forecast_fragment, menu);
	}


	@Override
	public void onResume() {
		super.onResume();
		if (mLocation != null && !mLocation.equals(new PreferredLocationFetcher().getPreferredLocation(getActivity()))) {
			getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
		}
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
		String zipcode = new PreferredLocationFetcher().getPreferredLocation(getActivity());
		FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
		weatherTask.execute(zipcode);
	}



	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		this.setHasOptionsMenu(true);
		getLoaderManager().initLoader(FORECAST_LOADER, null, this);
		super.onActivityCreated(savedInstanceState);
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
		listViewAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.list_item_forecast,
				null,
				//column names from cursor
				new String[]{WeatherEntry.COLUMN_DATETEXT,
						WeatherEntry.COLUMN_SHORT_DESC,
						WeatherEntry.COLUMN_MAX_TEMP,
						WeatherEntry.COLUMN_MIN_TEMP},
				//id's of the textviews to dump data into
				new int[] {R.id.list_item_date_textview,
						R.id.list_item_forecast_textview,
						R.id.list_item_high_textview,
						R.id.list_item_low_textview},
				0
				);
		listview.setAdapter(listViewAdapter);
	}


	private void setupOnClickListener(final ListView listview){

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			    SimpleCursorAdapter adapter = (SimpleCursorAdapter)	parent.getAdapter();
				Cursor cursor = adapter.getCursor();

				if(cursor != null && cursor.moveToPosition(position)){
					Intent explicitIntent = new Intent(getActivity(), DetailActivity.class);

					String dateString = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT));

					explicitIntent.putExtra(DetailActivity.IntentExtras.ForecastDate, dateString);
					getActivity().startActivity(explicitIntent);
				}
			}
		});

	}


	@Override
	public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.  This
		// fragment only uses one loader, so we don't care about checking the id.

		// To only show current and future dates, get the String representation for today,
		// and filter the query to return weather only for dates after or including today.
		// Only return data after today.
		String startDate = WeatherContract.getDbDateString(new Date());

		// Sort order:  Ascending, by date.
		String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

		mLocation = new PreferredLocationFetcher().getPreferredLocation(getActivity());
		Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
				mLocation, startDate);

		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(
				getActivity(),
				weatherForLocationUri,
				FORECAST_COLUMNS,
				null,
				null,
				sortOrder
		);
	}


	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
		listViewAdapter.swapCursor(data);
	}


	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
		listViewAdapter.swapCursor(null);
	}
}
