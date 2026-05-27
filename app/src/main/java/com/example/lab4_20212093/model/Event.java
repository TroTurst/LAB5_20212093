package com.example.lab4_20212093.model;

public class Event {
    private int id;
    private String name;
    private int day;
    private int month;
    private int year;
    private int hour;
    private int minute;
    private String periodicity;
    private int notifyDaysBefore;

    public Event() {
    }

    public Event(int id, String name, int day, int month, int year, int hour, int minute,
                 String periodicity, int notifyDaysBefore) {
        this.id = id;
        this.name = name;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
        this.periodicity = periodicity;
        this.notifyDaysBefore = notifyDaysBefore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public int getNotifyDaysBefore() {
        return notifyDaysBefore;
    }

    public void setNotifyDaysBefore(int notifyDaysBefore) {
        this.notifyDaysBefore = notifyDaysBefore;
    }
}

