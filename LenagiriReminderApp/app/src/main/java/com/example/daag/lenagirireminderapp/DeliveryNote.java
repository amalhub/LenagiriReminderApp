package com.example.daag.lenagirireminderapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Created by daag on 7/23/17.
 */

public class DeliveryNote extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String number = intent.getStringExtra("sent-to");
        switch (getResultCode()) {
            case Activity.RESULT_OK:
                Log.d(MainActivity.LOG_TAG, "SMS Delivery success: " + number);
                setMyNotification(context, "SMS Success", number);
                break;

            case Activity.RESULT_CANCELED:

            default:
                Log.d(MainActivity.LOG_TAG, "SMS Delivery Failed: " + number);
                setMyNotification(context, "SMS Failed", number);
                break;
        }
    }

    public void setMyNotification(Context context, String title, String message) {
        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(), 0);
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(001, notification);
    }
}
