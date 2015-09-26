package com.example.brett.sunshine.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.example.brett.sunshine.MainActivity;
import com.example.brett.sunshine.R;
import com.example.brett.sunshine.WeatherFormatHelper;

public final class TodayWidgetProvider extends AppWidgetProvider{

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        int weatherArtResourceId = R.drawable.art_clear;
        String weatherDescription = "Clear";
        double maxTemp = 24;
        String formattedMaxTemperature = new WeatherFormatHelper().formatTemperature(context, maxTemp, true);

        for(int appWidgetId : appWidgetIds){

            int layoutId = R.layout.widget_today_small;
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);

            remoteViews.setImageViewResource(R.id.widget_icon, weatherArtResourceId);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
                setRemoteContentDescription(remoteViews, weatherDescription);
            }

            remoteViews.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);


            Intent launchIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingLaunchIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget, pendingLaunchIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        }

    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews remoteViews, String description) {
    remoteViews.setContentDescription(R.id.widget_icon, description);
    }

}
