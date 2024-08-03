package com.example.my_fitness_tracker_5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WorkoutAdapter extends ArrayAdapter<Workout> {

    private static final String TAG = "WorkoutAdapter";
    private final Context context;
    private final ArrayList<Workout> workoutsList;

    public WorkoutAdapter(Context context, ArrayList<Workout> workoutsList) {
        super(context, 0, workoutsList);
        this.context = context;
        this.workoutsList = workoutsList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.workout_item, parent, false);
        }

        Workout workout = workoutsList.get(position);

        TextView textViewDescription = convertView.findViewById(R.id.textView_description);
        ImageView imageViewPhoto = convertView.findViewById(R.id.imageView_photo);
        ImageView imageViewDelete = convertView.findViewById(R.id.imageView_delete);

        textViewDescription.setText(workout.getDescription());

        if (workout.getPhotoBase64() != null && !workout.getPhotoBase64().isEmpty()) {
            try {
                if (isValidBase64(workout.getPhotoBase64())) {
                    Bitmap bitmap = decodeBase64(workout.getPhotoBase64());
                    imageViewPhoto.setImageBitmap(bitmap);
                    imageViewPhoto.setVisibility(View.VISIBLE);

                    imageViewPhoto.setOnClickListener(v -> showImagePreview(workout.getPhotoBase64()));
                } else {
                    Log.e(TAG, "Invalid Base64 string");
                    imageViewPhoto.setVisibility(View.GONE);
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Base64 decoding failed", e);
                imageViewPhoto.setVisibility(View.GONE);
            }
        } else {
            imageViewPhoto.setVisibility(View.GONE);
        }

        imageViewDelete.setOnClickListener(v -> {
            workoutsList.remove(position);
            notifyDataSetChanged();
            saveWorkouts();
        });

        return convertView;
    }

    private Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    private void showImagePreview(String imageBase64) {
        FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
        ImagePreviewDialogFragment.newInstance(imageBase64).show(fragmentManager, "image_preview");
    }

    private void saveWorkouts() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> workoutsSet = new HashSet<>();
        for (Workout workout : workoutsList) {
            workoutsSet.add(workout.toString());
        }
        editor.putStringSet("workouts", workoutsSet);
        editor.apply();
    }

    // Utility method to validate Base64 string
    private boolean isValidBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Base64.decode(str, Base64.DEFAULT);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
