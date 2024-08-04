package com.example.my_fitness_tracker_5;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<String> goalsList;
    private final ArrayList<String> goalIds;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public GoalAdapter(Context context, ArrayList<String> goalsList, ArrayList<String> goalIds, FirebaseFirestore db) {
        this.context = context;
        this.goalsList = goalsList;
        this.goalIds = goalIds;
        this.db = db;
        this.mAuth = FirebaseAuth.getInstance();
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
        holder.imageViewDelete.setOnClickListener(v -> {
            String goalId = goalIds.get(position);
            db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .collection("goals").document(goalId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        goalsList.remove(position);
                        goalIds.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Goal successfully deleted!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error deleting goal", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return goalsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewGoal;
        ImageView imageViewDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGoal = itemView.findViewById(R.id.textView_goal);
            imageViewDelete = itemView.findViewById(R.id.imageView_delete);
        }
    }
}
