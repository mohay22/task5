package com.example.task5;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList;
    private FirebaseFirestore db;
    private Button btnAddReminder, testNotificationButton;
    private static final String TAG = "MainActivity"; // For logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewReminders);
        btnAddReminder = findViewById(R.id.btnAddReminder);
        testNotificationButton = findViewById(R.id.testNotificationButton); // Added test button

        Log.d(TAG, "MainActivity onCreate called");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reminderList = new ArrayList<>();
        adapter = new ReminderAdapter(reminderList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadReminders();

        // Fix: Add Reminder button now opens AddReminderActivity
        btnAddReminder.setOnClickListener(v -> {
            Log.d(TAG, "Add Reminder button clicked");
            // Navigating to AddReminderActivity
            Intent intent = new Intent(MainActivity.this, AddReminderActivity.class);
            startActivity(intent);
        });

        // Request notification permissions for Android 13+
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        // Test notification manually
        testNotificationButton.setOnClickListener(v -> {
            Log.d(TAG, "Sending test broadcast");
            Intent intent = new Intent(MainActivity.this, NotificationReceiver.class);
            intent.setAction("com.example.task5.NOTIFY"); // Explicit intent action
            intent.putExtra("title", "Test Reminder");
            intent.putExtra("important", true);
            sendBroadcast(intent);
        });
    }
    private void loadReminders() {
        Log.d(TAG, "Loading reminders from Firestore");

        db.collection("reminders").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reminderList.clear();
                    Log.d(TAG, "Successfully loaded reminders from Firestore");

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Convert the document to a Reminder object
                        Reminder reminder = doc.toObject(Reminder.class);
                        reminder.setId(doc.getId());
                        reminderList.add(reminder);

                        // Log to check if the description is fetched properly
                        Log.d(TAG, "Loaded Reminder: " + reminder.getTitle() + " | Description: " + reminder.getDescription());

                        // Ensure timestamp is valid
                        if (reminder.getTimestamp() > System.currentTimeMillis()) {
                            Log.d(TAG, "Scheduling notification for: " + reminder.getTitle());
                            // Now passing title, description, and timestamp
                            scheduleNotification(reminder.getTitle(), reminder.getDescription(), reminder.getTimestamp());
                        } else {
                            Log.d(TAG, "Skipping past reminder: " + reminder.getTitle());
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading reminders from Firestore", e));
    }


    private void scheduleNotification(String title, String description, long time) {
        Log.d(TAG, "Scheduling notification for: " + title + " at " + time);

        if (time <= System.currentTimeMillis()) {
            Log.e(TAG, "Cannot schedule notification for past time: " + time);
            return;
        }

        // Check if the app has permission to schedule exact alarms
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Check if the app can schedule exact alarms
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "Permission to schedule exact alarms not granted. Requesting permission...");
                // Request the user to grant permission to schedule exact alarms
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return; // Exit the method if permission is not granted
            }
        }

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.setAction("com.example.task5.NOTIFY");
        intent.putExtra("title", title);
        intent.putExtra("description", description); // Pass description to the intent
        intent.putExtra("important", true); // Assuming all reminders are important for now

        int requestCode = (int) (time % Integer.MAX_VALUE);
        if (requestCode < 0) {
            requestCode = Math.abs(requestCode);
        }
        Log.d(TAG, "Generated requestCode: " + requestCode);

        try {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (pendingIntent == null) {
                Log.e(TAG, "PendingIntent is null!");
                return;
            }

            if (alarmManager != null) {
                Log.d(TAG, "Setting alarm for: " + title + " at " + time);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                }
            } else {
                Log.e(TAG, "AlarmManager is null!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification", e);
        }
    }







}
