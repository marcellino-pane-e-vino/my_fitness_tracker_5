package com.example.my_fitness_tracker_5;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AddWorkoutActivity extends AppCompatActivity {

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String WORKOUTS_KEY = "workouts";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private Spinner spinnerSport;
    private EditText editTextDistanceReps;
    private Button buttonUploadPhoto, buttonTakePhoto, buttonConfirmWorkout;
    private ListView listViewWorkouts;
    private WorkoutAdapter workoutsAdapter;
    private ArrayList<Workout> workoutsList;
    private String currentPhotoBase64;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        context = this;
        spinnerSport = findViewById(R.id.spinner_sport);
        editTextDistanceReps = findViewById(R.id.editText_distance_reps);
        buttonUploadPhoto = findViewById(R.id.button_upload_photo);
        buttonTakePhoto = findViewById(R.id.button_take_photo);
        buttonConfirmWorkout = findViewById(R.id.button_confirm_workout);
        listViewWorkouts = findViewById(R.id.listView_workouts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add a Workout");

        workoutsList = new ArrayList<>();
        workoutsAdapter = new WorkoutAdapter(this, workoutsList);
        listViewWorkouts.setAdapter(workoutsAdapter);

        loadWorkouts();

        buttonUploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_PICK);
            }
        });

        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        buttonConfirmWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sport = spinnerSport.getSelectedItem().toString();
                String distanceReps = editTextDistanceReps.getText().toString();

                if (distanceReps.isEmpty()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                String workoutDescription = "Sport: " + sport + ", Distance/Reps: " + distanceReps;
                Workout workout = new Workout(workoutDescription, currentPhotoBase64);
                workoutsList.add(workout);
                workoutsAdapter.notifyDataSetChanged();

                saveWorkouts();
                setListViewHeightBasedOnChildren(listViewWorkouts);

                currentPhotoBase64 = null; // Reset the current photo after confirming
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // This will navigate back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                currentPhotoBase64 = encodeBase64(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri imageUri = data.getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    currentPhotoBase64 = encodeBase64(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String encodeBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveWorkouts() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> workoutsSet = new HashSet<>();
        for (Workout workout : workoutsList) {
            workoutsSet.add(workout.toString());
        }
        editor.putStringSet(WORKOUTS_KEY, workoutsSet);
        editor.apply();
    }

    private void loadWorkouts() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> workoutsSet = sharedPreferences.getStringSet(WORKOUTS_KEY, new HashSet<String>());
        workoutsList.clear();
        for (String workoutString : workoutsSet) {
            workoutsList.add(Workout.fromString(workoutString));
        }
        workoutsAdapter.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(listViewWorkouts);
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        ArrayAdapter adapter = (ArrayAdapter) listView.getAdapter();
        if (adapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
