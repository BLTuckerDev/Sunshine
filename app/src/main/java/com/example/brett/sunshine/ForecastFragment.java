package com.example.brett.sunshine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.brett.sunshine.data.WeatherContract;
import com.example.brett.sunshine.sync.SunshineSyncAdapter;

import java.util.Date;

import static com.example.brett.sunshine.data.WeatherContract.WeatherEntry;


public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int FORECAST_LOADER = 0;
    private String mLocation;
    private int selectedPosition;

    private RecyclerView recyclerView;
    private int position = RecyclerView.NO_POSITION;

    private ForecastAdapter forecastAdapter;

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
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.LocationEntry.COLUMN_LATITUDE,
            WeatherContract.LocationEntry.COLUMN_LONGITUDE
    };

    public ForecastFragment() {
        this.setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }


    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);

        if (mLocation != null && !mLocation.equals(new PreferredLocationFetcher().getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                this.launchWeatherTask();
                return true;
            case R.id.action_main_map_location:
                this.sendMapsIntent();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void launchWeatherTask() {
        SunshineSyncAdapter.initializeSyncAdapter(getActivity());
    }


    private void sendMapsIntent() {
        Intent mapsIntent = new Intent(Intent.ACTION_VIEW);
        Uri.Builder builder = new Uri.Builder();

        Cursor c = forecastAdapter.getWeatherCursor();

        if (c != null && c.moveToFirst()) {

            String latitude = c.getString(c.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LATITUDE));
            String longitude = c.getString(c.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LONGITUDE));

            Uri geoLocation = Uri.parse("geo:" + latitude + "," + longitude);

            mapsIntent.setData(geoLocation);
            if (mapsIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(mapsIntent);
            }

        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        forecastAdapter = new ForecastAdapter(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_forecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(forecastAdapter);

        // We'll call our MainActivity
//        mRecyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                // CursorAdapter returns a cursor at the correct position for getItem(), or null
//                // if it cannot seek to that position.
//                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
//                if (cursor != null) {
//                    String locationSetting = Utility.getPreferredLocation(getActivity());
//                    ((Callback) getActivity())
//                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
//                            ));
//                }
//                mPosition = position;
//            }
//        });



        if (savedInstanceState != null && savedInstanceState.containsKey("position")) {
            selectedPosition = savedInstanceState.getInt("position");
        }

        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("position", selectedPosition);
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
        forecastAdapter.swapCursor(data);
        if (selectedPosition != RecyclerView.NO_POSITION) {
            recyclerView.smoothScrollToPosition(selectedPosition);
        }
        updateEmptyViewStatusText();
    }


    private void updateEmptyViewStatusText() {

        TextView emptyView = (TextView) getView().findViewById(R.id.recyclerview_forecast_empty);

        if(null == emptyView){
            return;
        }


        if (forecastAdapter.getItemCount() == 0) {
                emptyView.setVisibility(View.VISIBLE);

                int message = R.string.no_weather_info_available;
                @SunshineSyncAdapter.LocationStatus int locationStatus = new LocationStatusPreferenceManager().getCurrentLocationStatus(getActivity());

                switch (locationStatus) {
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.empty_forecast_list_server_down;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.empty_forecast_list_server_error;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                        message = R.string.invalid_location_message;
                        break;
                    default:

                        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
                            message = R.string.no_network_available;
                        }
                }

                emptyView.setText(message);
        } else {
            //count is not 0
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }


    public void setUseTodayLayout(boolean useTodayLayout) {
        forecastAdapter.setUseTodayLayout(useTodayLayout);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.location_status_pref_key))) {
            updateEmptyViewStatusText();
        }
    }
}
