package com.example.daag.lenagirireminderapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    final static String LOG_TAG = "Lenagiri";
    List<SMSData> smsList;
    final static int hour = 20;
    final static int minute = 00;
    final static int seconds = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SingletonWorker worker = SingletonWorker.getInstance(this);

        if (worker.isAlarmSet()) {
            ((TextView) findViewById(R.id.alarmStatus)).setText("ON");
        } else {
            ((TextView) findViewById(R.id.alarmStatus)).setText("OFF");
        }
        ((TextView) findViewById(R.id.sendTimeLabel)).setText(hour + ":" + minute);

        smsList = worker.getNextSMSList();

        String smsData = "";
        for (SMSData sms: smsList) {
            smsData += sms.getNumbersToString() + "\n";
            smsData += sms.getName() + ": " + sms.getDate() + "\n\n";
        }
        ((TextView) findViewById(R.id.smsDetails)).setText(smsData);

        findViewById(R.id.toggleAlarmBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (worker.isAlarmSet()) {
                    worker.cancelAlarm();
                } else {
                    worker.setAlarm(hour, minute, seconds);
                }
                if (worker.isAlarmSet()) {
                    ((TextView) findViewById(R.id.alarmStatus)).setText("ON");
                } else {
                    ((TextView) findViewById(R.id.alarmStatus)).setText("OFF");
                }
            }
        });

        findViewById(R.id.sendNowBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (SMSData sms: smsList) {
                    worker.sendSMS(sms);
                }
                Toast.makeText(worker.getMainActivity(), "SMS Sent!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
