package com.example.daag.lenagirireminderapp;

/**
 * Created by daag on 7/23/17.
 */

public class SMSData {
    private String numbers[];
    private String name;
    private String type[];
    private String date;

    public String[] getNumbers() {
        return numbers;
    }

    public String getNumbersToString() {
        String numbersList = "";
        for (String number: numbers) {
            numbersList += number + ",";
        }
        return numbersList;
    }

    public void setNumbers(String[] numbers) {
        this.numbers = numbers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getType() {
        return type;
    }

    public void setType(String[] type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
