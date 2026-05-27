package com.example.lab4_20212093.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.lab4_20212093.model.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class EventDAO {
    private final DatabaseHelper dbHelper;

    public EventDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public int insertEvent(Event event) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toValues(event);
        long id = db.insert(DatabaseHelper.TABLE_EVENTS, null, values);
        db.close();
        return (int) id;
    }

    public int updateEvent(Event event) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toValues(event);
        int rows = db.update(DatabaseHelper.TABLE_EVENTS, values,
            DatabaseHelper.COL_ID + " = ?", new String[]{String.valueOf(event.getId())});
        db.close();
        return rows;
    }

    public int deleteEvent(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_EVENTS,
            DatabaseHelper.COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public List<Event> getAllEvents() {
        List<Event> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_EVENTS, null, null, null,
            null, null, DatabaseHelper.COL_YEAR + ", " + DatabaseHelper.COL_MONTH + ", " +
                DatabaseHelper.COL_DAY + ", " + DatabaseHelper.COL_HOUR + ", " +
                DatabaseHelper.COL_MINUTE);
        if (cursor.moveToFirst()) {
            do {
                list.add(fromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public Event getEventById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_EVENTS, null,
            DatabaseHelper.COL_ID + " = ?", new String[]{String.valueOf(id)},
            null, null, null);
        Event event = null;
        if (cursor.moveToFirst()) {
            event = fromCursor(cursor);
        }
        cursor.close();
        db.close();
        return event;
    }

    public List<Event> getEventsFromDate(int day, int month, int year) {
        List<Event> allEvents = getAllEvents();
        List<Event> result = new ArrayList<>();
        Calendar selected = Calendar.getInstance();
        selected.set(Calendar.YEAR, year);
        selected.set(Calendar.MONTH, month - 1);
        selected.set(Calendar.DAY_OF_MONTH, day);
        selected.set(Calendar.HOUR_OF_DAY, 0);
        selected.set(Calendar.MINUTE, 0);
        selected.set(Calendar.SECOND, 0);
        selected.set(Calendar.MILLISECOND, 0);

        long selectedMillis = selected.getTimeInMillis();
        for (Event event : allEvents) {
            long nextOccurrence = getNextOccurrenceMillis(event, selected);
            if (nextOccurrence >= selectedMillis) {
                result.add(event);
            }
        }

        Collections.sort(result, (left, right) -> Long.compare(
            getNextOccurrenceMillis(left, selected),
            getNextOccurrenceMillis(right, selected)
        ));
        return result;
    }

    private long getNextOccurrenceMillis(Event event, Calendar selected) {
        Calendar occurrence = Calendar.getInstance();
        int resolvedMonth = resolveEventMonth(event.getYear(), event.getMonth(), event.getDay());
        occurrence.set(Calendar.YEAR, event.getYear());
        occurrence.set(Calendar.MONTH, resolvedMonth - 1);
        occurrence.set(Calendar.DAY_OF_MONTH, event.getDay());
        int hour = event.getHour() >= 0 ? event.getHour() : 0;
        int minute = event.getMinute() >= 0 ? event.getMinute() : 0;
        occurrence.set(Calendar.HOUR_OF_DAY, hour);
        occurrence.set(Calendar.MINUTE, minute);
        occurrence.set(Calendar.SECOND, 0);
        occurrence.set(Calendar.MILLISECOND, 0);

        if ("ANNUAL".equals(event.getPeriodicity())) {
            occurrence.set(Calendar.YEAR, selected.get(Calendar.YEAR));
            if (occurrence.before(selected)) {
                occurrence.add(Calendar.YEAR, 1);
            }
        }

        return occurrence.getTimeInMillis();
    }

    private int resolveEventMonth(int year, int month, int day) {
        if (!isValidDate(year, month, day) && isValidDate(year, month + 1, day)) {
            return month + 1;
        }
        return month;
    }

    private boolean isValidDate(int year, int month, int day) {
        if (month < 1 || month > 12 || day < 1) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        try {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.getTime();
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private ContentValues toValues(Event event) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NAME, event.getName());
        values.put(DatabaseHelper.COL_DAY, event.getDay());
        values.put(DatabaseHelper.COL_MONTH, event.getMonth());
        values.put(DatabaseHelper.COL_YEAR, event.getYear());
        values.put(DatabaseHelper.COL_HOUR, event.getHour());
        values.put(DatabaseHelper.COL_MINUTE, event.getMinute());
        values.put(DatabaseHelper.COL_PERIODICITY, event.getPeriodicity());
        values.put(DatabaseHelper.COL_NOTIFY_DAYS, event.getNotifyDaysBefore());
        return values;
    }

    private Event fromCursor(Cursor cursor) {
        Event event = new Event();
        event.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID)));
        event.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME)));
        event.setDay(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DAY)));
        event.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MONTH)));
        event.setYear(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_YEAR)));
        event.setHour(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_HOUR)));
        event.setMinute(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MINUTE)));
        event.setPeriodicity(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PERIODICITY)));
        event.setNotifyDaysBefore(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFY_DAYS)));
        return event;
    }
}
