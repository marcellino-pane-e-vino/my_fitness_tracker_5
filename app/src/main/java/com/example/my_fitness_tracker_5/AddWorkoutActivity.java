package com.example.my_fitness_tracker_5;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import com.jjoe64.graphview.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class AddWorkoutActivity extends AppCompatActivity {

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String WORKOUTS_KEY = "workouts";
    private static final String TAG = "AddWorkoutActivity";

    private Spinner spinnerSport;
    private EditText editTextDistanceReps;
    private ListView listViewWorkouts;
    private WorkoutAdapter workoutsAdapter;
    private ArrayList<Workout> workoutsList;
    private String currentPhotoBase64;
    private ImageView imageViewPhotoPreview;

    private Context context;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;
    private ActivityResultLauncher<Intent> pickPhotoLauncher;

    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        context = this;
        spinnerSport = findViewById(R.id.spinner_sport);
        editTextDistanceReps = findViewById(R.id.editText_distance_reps);
        Button buttonUploadPhoto = findViewById(R.id.button_upload_photo);
        Button buttonTakePhoto = findViewById(R.id.button_take_photo);
        Button buttonConfirmWorkout = findViewById(R.id.button_confirm_workout);
        listViewWorkouts = findViewById(R.id.listView_workouts);
        imageViewPhotoPreview = findViewById(R.id.imageView_photo_preview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add a Workout");

        workoutsList = new ArrayList<>();
        workoutsAdapter = new WorkoutAdapter(this, workoutsList);
        listViewWorkouts.setAdapter(workoutsAdapter);

        loadWorkouts();

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                openCamera();
            } else {
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        });

        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                File photoFile = new File(currentPhotoPath);
                if (photoFile.exists()) {
                    Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    if (imageBitmap != null) {
                        currentPhotoBase64 = encodeBase64(imageBitmap);
                        imageViewPhotoPreview.setImageBitmap(imageBitmap);
                        imageViewPhotoPreview.setVisibility(View.VISIBLE);
                    } else {
                        Log.e(TAG, "Decoded bitmap is null");
                        Toast.makeText(context, "Error: Decoded bitmap is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Photo file not found: " + currentPhotoPath);
                    Toast.makeText(context, "Error: Photo file not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Camera activity result not OK");
            }
        });

        pickPhotoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                assert result.getData() != null;
                Uri imageUri = result.getData().getData();
                try {
                    Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    currentPhotoBase64 = encodeBase64(imageBitmap);
                    imageViewPhotoPreview.setImageBitmap(imageBitmap);
                    imageViewPhotoPreview.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "Gallery activity result not OK");
            }
        });

        buttonUploadPhoto.setOnClickListener(v -> openGallery());

        buttonTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else {
                openCamera();
            }
        });

        buttonConfirmWorkout.setOnClickListener(v -> {
            String sport = spinnerSport.getSelectedItem().toString();
            String distanceReps = editTextDistanceReps.getText().toString();

            if (distanceReps.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String workoutDescription = "Sport: " + sport + ", Distance/Reps: " + distanceReps;

            // Validate the Base64 string before creating the Workout object
            if (currentPhotoBase64 != null && !isValidBase64(currentPhotoBase64)) {
                Log.e("AddWorkoutActivity", "Invalid Base64 string");
                currentPhotoBase64 = null; // Reset the current photo if invalid
            }

            Workout workout = new Workout(workoutDescription, currentPhotoBase64, new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            workoutsList.add(workout);
            workoutsAdapter.notifyDataSetChanged();

            saveWorkouts();
            setListViewHeightBasedOnChildren(listViewWorkouts);

            currentPhotoBase64 = null; // Reset the current photo after confirming
            imageViewPhotoPreview.setVisibility(View.GONE); // Hide the photo preview
            editTextDistanceReps.setText(""); // Clear the EditText
        });


        imageViewPhotoPreview.setOnClickListener(v -> {
            if (currentPhotoBase64 != null) {
                showImagePreview(currentPhotoBase64);
            }
        });
    }


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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // This will navigate back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoLauncher.launch(intent);
    }

    private void openCamera() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e(TAG, "Error creating photo file: ", ex);
                    Toast.makeText(context, "Error creating photo file", Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.my_fitness_tracker_5.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePhotoLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(context, "No camera app found. Please install a camera app.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "No camera available on this device", Toast.LENGTH_SHORT).show();
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
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
        Set<String> workoutsSet = sharedPreferences.getStringSet(WORKOUTS_KEY, new HashSet<>());
        workoutsList.clear();
        for (String workoutString : workoutsSet) {
            Workout workout = Workout.fromString(workoutString);
            if (workout != null) {
                workoutsList.add(workout);
            }
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

    private void showImagePreview(String imageBase64) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ImagePreviewDialogFragment.newInstance(imageBase64).show(fragmentManager, "image_preview");
    }
}
