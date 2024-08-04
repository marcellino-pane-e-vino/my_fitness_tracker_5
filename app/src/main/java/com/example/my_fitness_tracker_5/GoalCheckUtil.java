package com.example.my_fitness_tracker_5;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    public void checkGoalCompletion(String sport, double totalDistanceReps) {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("goals")
                .whereEqualTo("sport", sport)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String goalDistanceReps = document.getString("distanceReps");
                        if (goalDistanceReps != null && totalDistanceReps >= Double.parseDouble(goalDistanceReps)) {
                            sendNotification("Goal Achieved", "You have reached your goal: " + document.getString("sport") + ", " + document.getString("distanceReps") + " " + document.getString("date"));
                            db.collection("users").document(uid).collection("goals").document(document.getId()).delete();
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
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Goal Notification";
            String description = "Channel for goal notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
