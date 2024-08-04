package com.example.my_fitness_tracker_5;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> goalsList;

    public GoalAdapter(Context context, ArrayList<String> goalsList) {
        this.context = context;
        this.goalsList = goalsList;
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
            goalsList.remove(position);
            notifyItemRemoved(position);
            // Optionally, save changes to persistent storage here
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
