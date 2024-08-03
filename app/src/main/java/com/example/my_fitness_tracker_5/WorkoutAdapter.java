package com.example.my_fitness_tracker_5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WorkoutAdapter extends ArrayAdapter<Workout> {

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String WORKOUTS_KEY = "workouts";

    private Context context;
    private ArrayList<Workout> workoutsList;

    public WorkoutAdapter(Context context, ArrayList<Workout> workoutsList) {
        super(context, 0, workoutsList);
        this.context = context;
        this.workoutsList = workoutsList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_workout, parent, false);
        }

        TextView textViewWorkout = convertView.findViewById(R.id.textView_workout);
        ImageView imageViewPhoto = convertView.findViewById(R.id.imageView_photo);
        ImageButton buttonDeleteWorkout = convertView.findViewById(R.id.button_delete_workout);

        final Workout workout = getItem(position);
        textViewWorkout.setText(workout.getDescription());

        if (workout.getPhoto() != null) {
            Bitmap bitmap = decodeBase64(workout.getPhoto());
            imageViewPhoto.setImageBitmap(bitmap);
        }

        buttonDeleteWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workoutsList.remove(position);
                notifyDataSetChanged();
                saveWorkouts();
            }
        });

        return convertView;
    }

    private void saveWorkouts() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> workoutsSet = new HashSet<>();
        for (Workout workout : workoutsList) {
            workoutsSet.add(workout.toString());
        }
        editor.putStringSet(WORKOUTS_KEY, workoutsSet);
        editor.apply();
    }

    private Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
