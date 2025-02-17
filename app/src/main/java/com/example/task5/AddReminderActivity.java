package com.example.task5;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddReminderActivity extends AppCompatActivity {
    private EditText etTitle, etDescription;
    private Button btnSelectDate, btnSelectTime, btnSaveReminder;
    private RadioGroup radioGroupPriority;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    private FirebaseFirestore db;
    private Calendar selectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnSaveReminder = findViewById(R.id.btnSaveReminder);
        radioGroupPriority = findViewById(R.id.radioGroupPriority);
        db = FirebaseFirestore.getInstance();
        selectedDateTime = Calendar.getInstance();

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());

        btnSaveReminder.setOnClickListener(v -> saveReminder());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = dayOfMonth;
            selectedDateTime.set(year, month, dayOfMonth);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private void saveReminder() {
        String title = etTitle.getText().toString();
        String description = etDescription.getText().toString();
        boolean isImportant = radioGroupPriority.getCheckedRadioButtonId() == R.id.rbImportant;

        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = selectedDateTime.getTimeInMillis();

        // Check if the timestamp is in the future
        if (timestamp <= System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> reminder = new HashMap<>();
        reminder.put("title", title);
        reminder.put("description", description);
        reminder.put("timestamp", timestamp);
        reminder.put("important", isImportant);

        // Save the reminder to Firestore
        db.collection("reminders").add(reminder)
                .addOnSuccessListener(documentReference -> {
                    scheduleNotification(title, isImportant, timestamp,description);  // Schedule notification after saving
                    Toast.makeText(this, "Reminder saved", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after saving
                });
    }

    private void scheduleNotification(String title, boolean isImportant, long time, String description) {
        int requestCode = (int) System.currentTimeMillis(); // Unique ID

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("important", isImportant);
        intent.putExtra("description", description); // Use lowercase "description"

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            Log.d("AlarmManager", "Alarm set for: " + new Date(time).toString());
        }
    } }
