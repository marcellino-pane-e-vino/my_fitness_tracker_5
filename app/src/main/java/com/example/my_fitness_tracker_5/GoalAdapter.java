package com.example.my_fitness_tracker_5;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GoalAdapter extends ArrayAdapter<String> {

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String GOALS_KEY = "goals";

    private final Context context;
    private final ArrayList<String> goalsList;

    public GoalAdapter(Context context, ArrayList<String> goalsList) {
        super(context, 0, goalsList);
        this.context = context;
        this.goalsList = goalsList;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_goal, parent, false);
        }

        TextView textViewGoal = convertView.findViewById(R.id.textView_goal);
        ImageButton buttonDelete = convertView.findViewById(R.id.button_delete);

        final String goal = getItem(position);
        textViewGoal.setText(goal);

        buttonDelete.setOnClickListener(v -> {
            goalsList.remove(position);
            notifyDataSetChanged();
            saveGoals();
        });

        return convertView;
    }

    private void saveGoals() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> goalsSet = new HashSet<>(goalsList);
        editor.putStringSet(GOALS_KEY, goalsSet);
        editor.apply();
    }
}
