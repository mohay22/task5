package com.example.task5;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_IMPORTANT = "channel_important";
    private static final String CHANNEL_GENERAL = "channel_general";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver", "onReceive() called");

        if (intent == null) {
            Log.e("NotificationReceiver", "Received null intent!");
            return;
        }

        String action = intent.getAction();
        Log.d("NotificationReceiver", "Intent action: " + action);

        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description"); // Retrieve description
        boolean isImportant = intent.getBooleanExtra("important", false);

        Log.d("NotificationReceiver", "Received intent for notification: " + title);
        Log.d("NotificationReceiver", "Description: " + description); // Log description
        Log.d("NotificationReceiver", "Is Important: " + isImportant);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannelsIfNeeded(notificationManager);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, isImportant ? CHANNEL_IMPORTANT : CHANNEL_GENERAL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title) // Set title
                .setContentText(description) // Set description
                .setPriority(isImportant ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true); // Automatically dismiss the notification when tapped

        if (isImportant) {
            // Important notifications: Vibration and Sound
            builder.setVibrate(new long[]{0, 500, 500, 500}); // Vibrate pattern
            builder.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI); // Default notification sound
        }

        // Notify the user
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannelsIfNeeded(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
            NotificationChannel importantChannel = new NotificationChannel(
                    CHANNEL_IMPORTANT,
                    "Important Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            importantChannel.setDescription("This channel is for important reminder notifications.");
            importantChannel.setVibrationPattern(new long[]{0, 500, 500, 500}); // Vibrate pattern
            importantChannel.setSound(
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
            );

            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_GENERAL,
                    "General Reminders",
                    NotificationManager.IMPORTANCE_LOW
            );
            generalChannel.setDescription("This channel is for normal reminder notifications.");

            manager.createNotificationChannel(importantChannel);
            manager.createNotificationChannel(generalChannel);
        }
    }
}
