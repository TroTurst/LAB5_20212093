package com.example.lab4_20212093.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.lab4_20212093.R;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String eventName = intent.getStringExtra("eventName");
        String eventDate = intent.getStringExtra("eventDate");
        String periodicity = intent.getStringExtra("periodicity");
        int eventId = intent.getIntExtra("eventId", 0);

        String channelId = NotificationHelper.CHANNEL_ONCE;
        if ("ANNUAL".equals(periodicity)) {
            channelId = NotificationHelper.CHANNEL_ANNUAL;
        }

        String contentText = "El evento ocurrira el " + eventDate;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(eventName)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(eventId, builder.build());
    }
}

