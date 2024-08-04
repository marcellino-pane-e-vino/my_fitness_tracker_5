package com.example.my_fitness_tracker_5;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> goalsList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public GoalAdapter(Context context, ArrayList<String> goalsList) {
        this.context = context;
        this.goalsList = goalsList;
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

        calculateProgress(sport, distanceReps, holder.progressBar);

        holder.imageViewDelete.setOnClickListener(v -> {
            goalsList.remove(position);
            notifyItemRemoved(position);
            deleteGoalFromFirestore(goal);
        });
    }

    @Override
    public int getItemCount() {
        return goalsList.size();
    }

    private void calculateProgress(String sport, double goalDistanceReps, ProgressBar progressBar) {
        String uid = mAuth.getCurrentUser().getUid();
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
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to calculate progress", Toast.LENGTH_SHORT).show());
    }

    private void deleteGoalFromFirestore(String goal) {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("goals")
                .whereEqualTo("goal", goal)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        db.collection("users").document(uid).collection("goals").document(document.getId()).delete()
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Goal deleted", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete goal", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to find goal", Toast.LENGTH_SHORT).show());
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
}
