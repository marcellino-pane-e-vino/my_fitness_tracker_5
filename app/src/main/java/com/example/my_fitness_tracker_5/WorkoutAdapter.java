package com.example.my_fitness_tracker_5;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WorkoutAdapter extends ArrayAdapter<Workout> {

    private Context context;
    private ArrayList<Workout> workoutsList;

    public WorkoutAdapter(Context context, ArrayList<Workout> workoutsList) {
        super(context, 0, workoutsList);
        this.context = context;
        this.workoutsList = workoutsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.workout_item, parent, false);
        }

        Workout workout = workoutsList.get(position);

        TextView textViewDescription = convertView.findViewById(R.id.textView_description);
        ImageView imageViewPhoto = convertView.findViewById(R.id.imageView_photo);
        ImageView imageViewDelete = convertView.findViewById(R.id.imageView_delete);

        textViewDescription.setText(workout.getDescription());

        if (workout.getPhotoBase64() != null && !workout.getPhotoBase64().isEmpty()) {
            Bitmap bitmap = decodeBase64(workout.getPhotoBase64());
            imageViewPhoto.setImageBitmap(bitmap);
            imageViewPhoto.setVisibility(View.VISIBLE);

            imageViewPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showImagePreview(workout.getPhotoBase64());
                }
            });
        } else {
            imageViewPhoto.setVisibility(View.GONE);
        }

        imageViewDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workoutsList.remove(position);
                notifyDataSetChanged();
                saveWorkouts();
            }
        });

        return convertView;
    }

    private Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return android.graphics.BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
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
}
