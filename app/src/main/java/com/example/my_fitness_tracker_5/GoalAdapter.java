package com.example.my_fitness_tracker_5;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    private static final String CHANNEL_ID = "goal_notification_channel";
    private static final String TAG = "GoalAdapter";
    private final Context context;
    private final ArrayList<String> goalsList;
    private final ArrayList<String> goalIds; // Initialize goalIds in constructor
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public GoalAdapter(Context context, ArrayList<String> goalsList, ArrayList<String> goalIds) {
        this.context = context;
        this.goalsList = goalsList;
        this.goalIds = goalIds; // Initialize goalIds
        createNotificationChannel();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.goal_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String goal = goalsList.get(position);
        holder.textViewGoal.setText(goal);

        // Calculate progress for the goal
        String[] goalParts = goal.split(", ");
        String sport = goalParts[0].split(": ")[1];
        String distanceRepsStr = goalParts[1].split(": ")[1];
        double distanceReps = Double.parseDouble(distanceRepsStr);

        calculateProgress(sport, distanceReps, holder.progressBar, position);

        holder.imageViewDelete.setOnClickListener(v -> deleteGoalFromFirestore(position));
    }

    @Override
    public int getItemCount() {
        return goalsList.size();
    }

    private void calculateProgress(String sport, double goalDistanceReps, ProgressBar progressBar, int position) {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db.collection("users").document(uid).collection("workouts")
                .whereEqualTo("sport", sport)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalDistanceReps = 0;
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String distanceRepsStr = document.getString("distanceReps");
                        if (distanceRepsStr != null) {
                            totalDistanceReps += Double.parseDouble(distanceRepsStr);
                        }
                    }
                    int progress = (int) ((totalDistanceReps / goalDistanceReps) * 100);
                    progressBar.setProgress(progress);

                    if (progress >= 100) {
                        sendNotification("You have reached your goal: " + goalsList.get(position));
                        sendToast("Goal Achieved: " + goalsList.get(position));
                        deleteGoalFromFirestore(position);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to calculate progress", Toast.LENGTH_SHORT).show());
    }

    private void deleteGoalFromFirestore(int position) {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        String goalId = goalIds.get(position);
        db.collection("users").document(uid).collection("goals").document(goalId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Goal deleted", Toast.LENGTH_SHORT).show();
                    goalsList.remove(position);
                    goalIds.remove(position);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete goal", Toast.LENGTH_SHORT).show());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGoal;
        ImageView imageViewDelete;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGoal = itemView.findViewById(R.id.textView_goal);
            imageViewDelete = itemView.findViewById(R.id.imageView_delete);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_check_circle_24)
                .setContentTitle("Goal Achieved")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d(TAG, "Notification sent: " + message);
        } else {
            Log.e(TAG, "NotificationManager is null");
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

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            } else {
                Log.e(TAG, "Failed to create notification channel");
            }
        }
    }
}
