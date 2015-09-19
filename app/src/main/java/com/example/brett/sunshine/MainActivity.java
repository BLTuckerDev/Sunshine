package com.example.brett.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.brett.sunshine.sync.SunshineSyncAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements ForecastFragmentCallbackListener {

	private final String LOG_TAG = MainActivity.class.getSimpleName();

    static final String SENDER_ID = "778689306788";

    private static final String PROPERTY_APP_VERSION = "appVersion";

    public static final String PROPERTY_REG_ID = "registration_id";

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private boolean isTwoPaneMode = false;

    private GoogleCloudMessaging googleCloudMessaging;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

		if(findViewById(R.id.weather_detail_container) != null){
			isTwoPaneMode = true;
			if(savedInstanceState == null){
				getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, new DetailFragment()).commit();
			}
		}

		ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
		forecastFragment.setUseTodayLayout(!isTwoPaneMode);

		SunshineSyncAdapter.initializeSyncAdapter(this);

        if(checkForPlayServices()){
            //api key: AIzaSyBsV-7dUagVmthw_zoR9u6wXrnIJAOXveA

            googleCloudMessaging = GoogleCloudMessaging.getInstance(this);
            String registrationId = getRegistrationId();

            if(registrationId.isEmpty()){
                registerGcmInBackground();
            }



        } else {
            storeRegistrationId(null);
            //TODO show snack bar error
        }

	}


	@Override
	protected void onStart() {
		super.onStart();
	}


	@Override
	protected void onResume() {
		super.onResume();
	}


	@Override
	protected void onPause() {
        super.onPause();
	}


	@Override
	protected void onStop() {
        super.onStop();
	}


	@Override
	protected void onDestroy() {
        super.onDestroy();
	}


	@Override
	protected void onRestart() {
        super.onRestart();
	}


    private boolean checkForPlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //We dont support this
                finish();
            }

            return false;

        }

        return true;
    }


    private String getRegistrationId(){
        final SharedPreferences gcmPreferences = getGcmPreferences();
        String registrationId = gcmPreferences.getString(PROPERTY_REG_ID, "");

        if(registrationId.isEmpty()){
            return "";
        }

        int registrationVersion = gcmPreferences.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();

        if(registrationVersion != currentVersion){
            return "";
        }

        return registrationId;
    }


    private SharedPreferences getGcmPreferences(){
        return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }


    private int getAppVersion(){
        try{
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch(PackageManager.NameNotFoundException ex){
            //wheeeeee
            Log.e(LOG_TAG, "Some how we failed to get package info. GG");
            throw new RuntimeException("Unable to get package info.");
        }
    }


    private void registerGcmInBackground(){

        final Context context = this;

        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {


                String message = "";

                try{

                    if(null == googleCloudMessaging){
                        googleCloudMessaging = GoogleCloudMessaging.getInstance(context);
                    }

                    String registrationId = googleCloudMessaging.register(SENDER_ID);
                    message = "Device Registered Successfully with ID = " + registrationId;

                    storeRegistrationId(registrationId);

                } catch(IOException ex){
                    Log.e(LOG_TAG, ex.getMessage());
                }

                return null;
            }
        }.execute(null, null, null);

    }


    private void storeRegistrationId(String registrationId){

        final SharedPreferences gcmPrefs = getGcmPreferences();
        int appVersion = getAppVersion();

        SharedPreferences.Editor  editor = gcmPrefs.edit();

        editor.putString(PROPERTY_REG_ID, registrationId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);

        editor.commit();
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

		return super.onOptionsItemSelected(item);
	}


	private void startSettingsActivity(){
		Intent settingsIntent = new Intent(this, SettingsActivity.class);
		startActivity(settingsIntent);
	}


	@Override
	public void onItemSelected(String date, ForecastListItemViewHolder viewHolder) {

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

            ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                    new Pair<View,String>(viewHolder.iconView, getString(R.string.detail_icon_transition_name)));
            ActivityCompat.startActivity(this, explicitIntent, activityOptions.toBundle());

		}
	}
}
