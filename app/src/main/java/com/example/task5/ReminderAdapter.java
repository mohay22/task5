package com.example.task5;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
    private List<Reminder> reminderList;

    // Constructor now takes List<Reminder>
    public ReminderAdapter(List<Reminder> reminderList) {
        this.reminderList = reminderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the reminder at the current position
        Reminder reminder = reminderList.get(position);

        // Set the title and description in the ViewHolder
        holder.title.setText(reminder.getTitle());
        holder.description.setText(reminder.getDescription()); // Add description field here

        // Get the timestamp from Firestore and convert it to a Date object
        long timestamp = reminder.getTimestamp(); // Assuming getTimestamp() returns a long

        // Convert the timestamp to a Date object
        Date date = new Date(timestamp);

        // Define the desired format for displaying the date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        // Format the date
        String formattedDate = dateFormat.format(date);

        // Set the formatted date in the dateTime TextView
        holder.dateTime.setText(formattedDate); // Display the formatted date
    }







    @Override
    public int getItemCount() {
        return reminderList != null ? reminderList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, dateTime, description;  // Add description here if needed

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.reminderTitle);
            dateTime = itemView.findViewById(R.id.reminderDateTime);  // Correct the reference here
            description = itemView.findViewById(R.id.reminderDescription);  // If you add description to your layout
        }
    }

}
