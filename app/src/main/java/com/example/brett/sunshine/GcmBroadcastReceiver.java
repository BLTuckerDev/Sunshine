package com.example.brett.sunshine;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public final class GcmBroadcastReceiver extends BroadcastReceiver {

    private final String LOG_TAG = BroadcastReceiver.class.getSimpleName();

    private static final String EXTRA_SENDER = "from";
    private static final String EXTRA_WEATHER = "weather";
    private static final String EXTRA_LOCATION = "location";

    public static final int SEVERE_WEATHER_NOTIFICATION_ID = 1;

    private NotificationManager notificationManager;


    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle intentExtras = intent.getExtras();
        GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(context);
        String messageType = googleCloudMessaging.getMessageType(intent);

        if(intentExtras.isEmpty()){
            return;
        }


        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

            if(MainActivity.SENDER_ID.equals(intentExtras.getString(EXTRA_SENDER))){

                String weather = intentExtras.getString(EXTRA_WEATHER);
                String location = intentExtras.getString(EXTRA_LOCATION);
                String alert = "Watch out! " + weather + " in " + location;

                sendNotification(context, alert);

            }

        }

    }


    private void sendNotification(Context context, String alert){

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.art_storm)
                .setContentText("Weather Alert!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(alert))
                .setContentText(alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        notificationBuilder.setContentIntent(pendingIntent);
        notificationManager.notify(SEVERE_WEATHER_NOTIFICATION_ID, notificationBuilder.build());


    }

}
