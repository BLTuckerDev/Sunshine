package com.example.brett.sunshine.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.example.brett.sunshine.LocationStatusPreferenceManager;
import com.example.brett.sunshine.MainActivity;
import com.example.brett.sunshine.PreferredLocationFetcher;
import com.example.brett.sunshine.R;
import com.example.brett.sunshine.WeatherFormatHelper;
import com.example.brett.sunshine.WeatherResourceConverter;
import com.example.brett.sunshine.data.WeatherContract;
import com.example.brett.sunshine.widget.TodayWidgetProvider;

import java.util.Date;

public final class TodayWidgetIntentService extends IntentService {


    public static final String[] FORECAST_COLUMNS = {
        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };


    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;
    private static final int INDEX_MAX_TEMP = 2;
    private static final int INDEX_MIN_TEMP = 3;

    public TodayWidgetIntentService(){
        super("TodayWidgetIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);

        int[] appWidgetIds = widgetManager.getAppWidgetIds(new ComponentName(this, TodayWidgetProvider.class));

        String location = new PreferredLocationFetcher().getPreferredLocation(this);
        String startDate = WeatherContract.getDbDateString(new Date());
        Uri weatherLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(location, startDate);

        Cursor data = getContentResolver().query(weatherLocationUri, FORECAST_COLUMNS, null,null, WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC");

        if(null == data){
            return;
        }

        if(!data.moveToFirst()){
            data.close();
            return;
        }

        int weatherId = data.getInt(INDEX_WEATHER_ID);
        int weatherArtResourceId = new WeatherResourceConverter().getArtResourceForWeatherCondition(weatherId);

        String weatherDescription = data.getString(INDEX_SHORT_DESC);

        WeatherFormatHelper formatHelper = new WeatherFormatHelper();
        double maxTemp = data.getDouble(INDEX_MAX_TEMP);
        String formattedMaxTemp = formatHelper.formatTemperature(this, maxTemp, false);

        double minTemp = data.getDouble(INDEX_MIN_TEMP);
        String formattedMinTemp = formatHelper.formatTemperature(this, minTemp, false);


        data.close();


        for(int appWidgetId : appWidgetIds){

            int widgetWidth = getWidgetWidth(widgetManager, appWidgetId);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);

            int layoutId;

            if(widgetWidth >= largeWidth){
                layoutId = R.layout.widget_today_large;
            } else if(widgetWidth >= defaultWidth){
                layoutId = R.layout.widget_today;
            } else {
                layoutId = R.layout.widget_today_small;
            }


            RemoteViews remoteViews = new RemoteViews(getPackageName(), layoutId);

            remoteViews.setImageViewResource(R.id.widget_high_temperature, weatherArtResourceId);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
                setRemoteContentDescription(remoteViews, weatherDescription);
            }

            remoteViews.setTextViewText(R.id.widget_description, weatherDescription);
            remoteViews.setTextViewText(R.id.widget_high_temperature, formattedMaxTemp);
            remoteViews.setTextViewText(R.id.widget_low_temperature, formattedMinTemp);

            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

            widgetManager.updateAppWidget(appWidgetId, remoteViews);


        }


    }


    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }

        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId){

        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        if(options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)){
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp, displayMetrics);
        }

        return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);

    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }
}
