package com.example.lab4_20212093.notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

import com.example.lab4_20212093.R;
import com.example.lab4_20212093.model.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationHelper {
    public static final String CHANNEL_ANNUAL = "channel_annual";
    public static final String CHANNEL_ONCE = "channel_once";

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel annualChannel = new NotificationChannel(
                CHANNEL_ANNUAL,
                context.getString(R.string.channel_annual_name),
                NotificationManager.IMPORTANCE_HIGH
            );
            NotificationChannel onceChannel = new NotificationChannel(
                CHANNEL_ONCE,
                context.getString(R.string.channel_once_name),
                NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(annualChannel);
            manager.createNotificationChannel(onceChannel);
        }
    }

    public static void scheduleNotification(Context ctx, Event event) {
        Calendar eventCalendar = Calendar.getInstance();
        eventCalendar.set(Calendar.YEAR, event.getYear());
        eventCalendar.set(Calendar.MONTH, event.getMonth() - 1);
        eventCalendar.set(Calendar.DAY_OF_MONTH, event.getDay());
        int hour = event.getHour() >= 0 ? event.getHour() : 9;
        int minute = event.getMinute() >= 0 ? event.getMinute() : 0;
        eventCalendar.set(Calendar.HOUR_OF_DAY, hour);
        eventCalendar.set(Calendar.MINUTE, minute);
        eventCalendar.set(Calendar.SECOND, 0);
        eventCalendar.set(Calendar.MILLISECOND, 0);

        Calendar now = Calendar.getInstance();
        if ("ANNUAL".equals(event.getPeriodicity()) && eventCalendar.before(now)) {
            eventCalendar.add(Calendar.YEAR, 1);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String eventDate = formatter.format(eventCalendar.getTime());

        eventCalendar.add(Calendar.DAY_OF_MONTH, -event.getNotifyDaysBefore());

        Intent intent = new Intent(ctx, NotificationReceiver.class);
        intent.putExtra("eventId", event.getId());
        intent.putExtra("eventName", event.getName());
        intent.putExtra("eventDate", eventDate);
        intent.putExtra("periodicity", event.getPeriodicity());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            ctx,
            event.getId(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        long triggerAtMillis = eventCalendar.getTimeInMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    triggerAtMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
            return;
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            triggerAtMillis, pendingIntent);
    }

    public static void cancelNotification(Context ctx, int eventId) {
        Intent intent = new Intent(ctx, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            ctx,
            eventId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        NotificationManagerCompat.from(ctx).cancel(eventId);
    }
}
