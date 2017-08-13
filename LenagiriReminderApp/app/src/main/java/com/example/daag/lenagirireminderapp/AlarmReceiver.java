package com.example.daag.lenagirireminderapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.List;

/**
 * Created by daag on 7/23/17.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SingletonWorker worker = SingletonWorker.getInstance();
        List<SMSData> smsList = worker.getNextSMSList();

        for (SMSData sms: smsList) {
            worker.sendSMS(sms);
        }
        Toast.makeText(context, "SMS Sent!", Toast.LENGTH_SHORT).show();
        SingletonWorker.getInstance().setAlarm(MainActivity.hour, MainActivity.minute, MainActivity.seconds);
    }
}
