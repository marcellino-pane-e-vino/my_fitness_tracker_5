package com.example.my_fitness_tracker_5;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GoalCheckUtil {

    private static final String CHANNEL_ID = "goal_notification_channel";
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private final Context context;

    public GoalCheckUtil(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        createNotificationChannel();
    }

    public void checkGoalCompletion(String sport, double distance) {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("goals")
                .whereEqualTo("sport", sport)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String goalDistanceReps = document.getString("distanceReps");
                        String goalDate = document.getString("expiryDate");
                        if (goalDistanceReps != null && goalDate != null && distance >= Double.parseDouble(goalDistanceReps)) {
                            // Check if the goal is within the expiry date
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            try {
                                Date goalExpiryDate = sdf.parse(goalDate);
                                if (goalExpiryDate != null && goalExpiryDate.after(new Date())) {
                                    sendNotification("Goal Achieved", "You have reached your goal: " + document.getString("sport") + ", " + document.getString("distanceReps") + " " + document.getString("expiryDate"));
                                    sendToast("Goal Achieved: " + document.getString("sport") + ", " + document.getString("distanceReps"));
                                    db.collection("users").document(uid).collection("goals").document(document.getId()).delete();
                                }
                            } catch (ParseException e) {
                                Log.e("GoalCheckUtil", "Failed to parse goal date", e);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("GoalCheckUtil", "Failed to check goals", e));
    }

    private void sendNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_check_circle_24)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d("GoalCheckUtil", "Notification sent: " + message);
        } else {
            Log.e("GoalCheckUtil", "NotificationManager is null");
        }
    }

    private void sendToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Goal Notification";
            String description = "Channel for goal notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d("GoalCheckUtil", "Notification channel created");
            } else {
                Log.e("GoalCheckUtil", "Failed to create notification channel");
            }
        }
    }
}
