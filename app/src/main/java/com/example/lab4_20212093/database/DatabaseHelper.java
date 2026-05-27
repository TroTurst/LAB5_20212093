package com.example.lab4_20212093.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "eventos.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_EVENTS = "eventos";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_DAY = "day";
    public static final String COL_MONTH = "month";
    public static final String COL_YEAR = "year";
    public static final String COL_HOUR = "hour";
    public static final String COL_MINUTE = "minute";
    public static final String COL_PERIODICITY = "periodicity";
    public static final String COL_NOTIFY_DAYS = "notify_days";

    private static final String CREATE_TABLE =
        "CREATE TABLE " + TABLE_EVENTS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_NAME + " TEXT NOT NULL, " +
            COL_DAY + " INTEGER NOT NULL, " +
            COL_MONTH + " INTEGER NOT NULL, " +
            COL_YEAR + " INTEGER NOT NULL, " +
            COL_HOUR + " INTEGER DEFAULT -1, " +
            COL_MINUTE + " INTEGER DEFAULT -1, " +
            COL_PERIODICITY + " TEXT NOT NULL, " +
            COL_NOTIFY_DAYS + " INTEGER DEFAULT 1" +
        ");";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }
}

