package com.example.brett.sunshine;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.brett.sunshine.data.WeatherContract;
import com.example.brett.sunshine.sync.SunshineSyncAdapter;

import java.util.Date;

import static com.example.brett.sunshine.data.WeatherContract.WeatherEntry;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

	private static final int FORECAST_LOADER = 0;
	private String mLocation;
	private int selectedPosition;

	private ListView listView;
	private ForecastAdapter listViewAdapter;

	private ForecastFragmentCallbackListener listener;

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
			WeatherEntry.COLUMN_WEATHER_ID,
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
		SunshineSyncAdapter.initializeSyncAdapter(getActivity());
	}



	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		this.setHasOptionsMenu(true);
		getLoaderManager().initLoader(FORECAST_LOADER, null, this);
		super.onActivityCreated(savedInstanceState);
	}


	@Override
	public void onAttach(Activity activity) {
		this.listener = (ForecastFragmentCallbackListener) activity;
		super.onAttach(activity);
	}


	@Override
	public void onDetach() {
		this.listener = null;
		super.onDetach();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		listView = (ListView) rootView.findViewById(R.id.listview_forecast);
		attachAdapter(listView);
		setupOnClickListener(listView);

		if(savedInstanceState != null && savedInstanceState.containsKey("position")){
			selectedPosition = savedInstanceState.getInt("position");
		}

		return rootView;
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("position", selectedPosition);
	}


	private void attachAdapter(ListView listview){
		listViewAdapter = new ForecastAdapter(getActivity(), null, 0);
		listview.setAdapter(listViewAdapter);
	}


	private void setupOnClickListener(final ListView listview){

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				if(ForecastFragment.this.listener != null){
					ForecastFragment.this.selectedPosition = position;
					ForecastAdapter adapter = (ForecastAdapter)	parent.getAdapter();
					Cursor cursor = adapter.getCursor();

					if(cursor != null && cursor.moveToPosition(position)){
						String dateString = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DATETEXT));
						ForecastFragment.this.listener.onItemSelected(dateString);
					}
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
				null,
				null,
				null,
				sortOrder
		);
	}


	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
		listViewAdapter.swapCursor(data);
		if(selectedPosition != ListView.INVALID_POSITION){
			listView.smoothScrollToPosition(selectedPosition);
		}
	}


	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
		listViewAdapter.swapCursor(null);
	}


	public void setUseTodayLayout(boolean useTodayLayout) {
		listViewAdapter.setUseTodayLayout(useTodayLayout);
	}
}
