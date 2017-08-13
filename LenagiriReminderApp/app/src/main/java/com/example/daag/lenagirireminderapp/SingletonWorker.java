package com.example.daag.lenagirireminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by daag on 8/13/17.
 */

public class SingletonWorker {
    private static MainActivity mainActivity;
    private static SingletonWorker worker;

    private SingletonWorker(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public static SingletonWorker getInstance(MainActivity mainActivity) {
        if (worker == null) {
            worker = new SingletonWorker(mainActivity);
        }
        return worker;
    }

    public static SingletonWorker getInstance() {
        return worker;
    }

    public boolean isAlarmSet() {
        Intent alarmIntent = new Intent(mainActivity, AlarmReceiver.class);
        return (PendingIntent.getBroadcast(mainActivity, 0, alarmIntent,
                PendingIntent.FLAG_NO_CREATE) != null);
    }

    public void cancelAlarm() {
        Intent alarmIntent = new Intent(mainActivity, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mainActivity, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) mainActivity.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public void setAlarm(int hour, int minute, int seconds) {
        final int DAY = 1000 * 60 * 60 * 24;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, seconds);
        long tempTime = calendar.getTimeInMillis();
        if (System.currentTimeMillis() >= tempTime){
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.setTimeInMillis(System.currentTimeMillis() + DAY);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, seconds);
        }

        Intent alarmIntent = new Intent(mainActivity, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mainActivity, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) mainActivity.getSystemService(Context.ALARM_SERVICE);

        manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(mainActivity, "Alarm Set: " + calendar.get(Calendar.DAY_OF_MONTH), Toast.LENGTH_SHORT).show();
    }

    public void sendSMS(SMSData smsData) {
        final String DELIVERY = "DELIVERY";

        for (String number: smsData.getNumbers()) {
            String message = "Dear " + smsData.getName().toString() + ", This is a kind reminder for your " +
                    "donation to Lenagiri temple alms giving on " + smsData.getDate().toString() +
                    ". Please acknowledge to 0773838047";

            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message.toString());
            Intent delivery = new Intent(DELIVERY);
            delivery.putExtra("sent-to", number);
            PendingIntent deliveryPI = PendingIntent.getBroadcast(mainActivity, 0, delivery, 0);
            ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();
            deliveryIntents.add(deliveryPI);
            mainActivity.registerReceiver(new DeliveryNote(), new IntentFilter(DELIVERY));
            smsManager.sendMultipartTextMessage(number, null, parts, null, deliveryIntents);
        }
    }

    public List<SMSData> getNextSMSList() {
        ArrayList<SMSData> smsList = new ArrayList<>();
        final int sevenDays = 1000 * 60 * 60 * 24 * 7;
        Date reminderDate = new Date(new Date().getTime() + sevenDays);
        DateFormat dateFormat = new SimpleDateFormat("MM");
        String month = dateFormat.format(reminderDate);
        dateFormat = new SimpleDateFormat("MMM");
        String monthName = dateFormat.format(reminderDate);
        dateFormat = new SimpleDateFormat("dd");
        String day = dateFormat.format(reminderDate);
        dateFormat = new SimpleDateFormat("yyyy");
        String year = dateFormat.format(reminderDate);
        Log.d(MainActivity.LOG_TAG, "Date: " + day + "-" + month);
        try {
            InputStream is = new FileInputStream(new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath(), "LenagiriSchedule.xml"));
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            doc.normalize();
            NodeList taskList = doc.getElementsByTagName("task");
            Node task = getNodeByAttribute(taskList, "date", day + "-" + month);
            if (task != null) {
                NodeList reminders = ((Element) task).getElementsByTagName("reminder");
                for (int i = 0; i < reminders.getLength(); i++) {
                    SMSData sms = new SMSData();
                    NodeList dataList = reminders.item(i).getChildNodes();
                    sms.setName(getNamedNode(dataList, "name").getTextContent());
                    sms.setNumbers(getNamedNode(dataList, "numbers").getTextContent().split(","));
                    sms.setType(reminders.item(i).getAttributes().getNamedItem("type")
                            .getTextContent().split(","));
                    sms.setDate(day + "/" + monthName + "/" + year);
                    smsList.add(sms);
                }
            }
        } catch (FileNotFoundException e) {
            ((TextView) mainActivity.findViewById(R.id.smsDetails)).setText("Data file not found!");
            Log.d(MainActivity.LOG_TAG, e.getMessage());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            ((TextView) mainActivity.findViewById(R.id.smsDetails)).setText("Problem reading the data file.");
            Log.d(MainActivity.LOG_TAG, e.getMessage());
        } catch (NullPointerException e) {
            ((TextView) mainActivity.findViewById(R.id.smsDetails)).setText("Missing data. Null Pointer");
            Log.d(MainActivity.LOG_TAG, e.getMessage());
        }
        return smsList;
    }

    private Node getNamedNode(NodeList nodeList, String name) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equalsIgnoreCase(name)) {
                    return node;
                }
            }
        }
        ((TextView) mainActivity.findViewById(R.id.smsDetails)).setText("Missing data!");
        Log.d(MainActivity.LOG_TAG, "Missing data");
        return null;
    }

    private Node getNodeByAttribute(NodeList nodeList, String attribute, String value) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getAttributes().getNamedItem(attribute).getTextContent()
                        .equalsIgnoreCase(value)) {
                    return node;
                }
            }
        }
        ((TextView) mainActivity.findViewById(R.id.smsDetails)).setText("Missing data!");
        Log.d(MainActivity.LOG_TAG, "Missing data");
        return null;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }
}
