package com.example.my_fitness_tracker_5;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class GoalCheckWorker extends Worker {

    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private final GoalCheckUtil goalCheckUtil;

    public GoalCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        goalCheckUtil = new GoalCheckUtil(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        String uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (uid == null) {
            Log.e("GoalCheckWorker", "User is not authenticated");
            return Result.failure();
        }

        db.collection("users").document(uid).collection("workouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalDistanceReps = 0;
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String distanceReps = document.getString("distanceReps");
                        if (distanceReps != null) {
                            totalDistanceReps += Double.parseDouble(distanceReps);
                        }
                    }
                    goalCheckUtil.checkGoalCompletion("Running", totalDistanceReps); // Adjust sport as needed
                })
                .addOnFailureListener(e -> Log.e("GoalCheckWorker", "Failed to load workouts", e));

        return Result.success();
    }
}
