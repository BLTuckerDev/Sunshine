package com.example.brett.sunshine.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.brett.sunshine.sync.SunshineSyncAdapter;

public final class TodayWidgetProvider extends AppWidgetProvider{

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            this.sendUpdateIntent(context);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.sendUpdateIntent(context);
    }


    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        this.sendUpdateIntent(context);
    }

    private void sendUpdateIntent(Context context){
        context.startService(new Intent(context, TodayWidgetIntentService.class));
    }

}
