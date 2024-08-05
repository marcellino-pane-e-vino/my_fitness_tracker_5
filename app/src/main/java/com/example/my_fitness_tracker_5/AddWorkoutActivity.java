package com.example.my_fitness_tracker_5;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AddWorkoutActivity extends AppCompatActivity {

    private Spinner spinnerSport;
    private EditText editTextDistanceReps;
    private TextView textViewSelectedDate;
    private ListView listViewWorkouts;
    private WorkoutAdapter workoutsAdapter;
    private ArrayList<Workout> workoutsList;
    private ArrayList<String> workoutIds;
    private String currentPhotoBase64;
    private ImageView imageViewPhotoPreview;
    private Button buttonSelectDate;
    private Button buttonUploadPhoto;
    private Button buttonTakePhoto;
    private Button buttonConfirmWorkout;

    private Context context;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;
    private ActivityResultLauncher<Intent> pickPhotoLauncher;

    private String currentPhotoPath;
    private String selectedDate;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private GoalCheckUtil goalCheckUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        context = this;
        goalCheckUtil = new GoalCheckUtil(context);
        initializeUIElements();
        setupActivityResultLaunchers();
        loadWorkouts();

        buttonSelectDate.setOnClickListener(v -> showDatePickerDialog());
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

            if (distanceReps.isEmpty() || selectedDate == null) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String workoutDescription = "Sport: " + sport + ", Distance/Reps: " + distanceReps;

            if (currentPhotoBase64 != null && !isValidBase64(currentPhotoBase64)) {
                Log.e("AddWorkoutActivity", "Invalid Base64 string");
                currentPhotoBase64 = null; // Reset the current photo if invalid
            }

            String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            Workout workout = new Workout(uid, sport, distanceReps, selectedDate, workoutDescription, currentPhotoBase64);
            workoutsList.add(workout);
            workoutsAdapter.notifyDataSetChanged();

            saveWorkoutToFirestore(sport, distanceReps, selectedDate, currentPhotoBase64);

            resetFields();
        });

        imageViewPhotoPreview.setOnClickListener(v -> {
            if (currentPhotoBase64 != null) {
                showImagePreview(currentPhotoBase64);
            }
        });
    }

    private void initializeUIElements() {
        spinnerSport = findViewById(R.id.spinner_sport);
        editTextDistanceReps = findViewById(R.id.editText_distance_reps);
        buttonSelectDate = findViewById(R.id.button_select_date);
        textViewSelectedDate = findViewById(R.id.textView_selected_date);
        buttonUploadPhoto = findViewById(R.id.button_upload_photo);
        buttonTakePhoto = findViewById(R.id.button_take_photo);
        buttonConfirmWorkout = findViewById(R.id.button_confirm_workout);
        listViewWorkouts = findViewById(R.id.listView_workouts);
        imageViewPhotoPreview = findViewById(R.id.imageView_photo_preview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add a Workout");
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        workoutsList = new ArrayList<>();
        workoutIds = new ArrayList<>();
        workoutsAdapter = new WorkoutAdapter(this, workoutsList, workoutIds, db);
        listViewWorkouts.setAdapter(workoutsAdapter);
    }

    private void setupActivityResultLaunchers() {
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
                        Log.e("AddWorkoutActivity", "Decoded bitmap is null");
                        Toast.makeText(context, "Error: Decoded bitmap is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("AddWorkoutActivity", "Photo file not found: " + currentPhotoPath);
                    Toast.makeText(context, "Error: Photo file not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("AddWorkoutActivity", "Camera activity result not OK");
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
                Log.e("AddWorkoutActivity", "Gallery activity result not OK");
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year1, month1, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            textViewSelectedDate.setText("Selected Date: " + selectedDate);
        }, year, month, day);
        datePickerDialog.show();
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
                File photoFile;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e("AddWorkoutActivity", "Error creating photo file: ", ex);
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private String encodeBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveWorkoutToFirestore(String sport, String distanceReps, String date, String photoBase64) {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Map<String, Object> workout = new HashMap<>();
        workout.put("uid", uid);
        workout.put("sport", sport);
        workout.put("distanceReps", distanceReps);
        workout.put("date", date);
        workout.put("photoBase64", photoBase64);

        db.collection("users").document(uid).collection("workouts").add(workout)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddWorkoutActivity.this, "Workout added", Toast.LENGTH_SHORT).show();
                    // Check for goal completion
                    goalCheckUtil.checkGoalCompletion(sport, Double.parseDouble(distanceReps));
                    // Schedule the GoalCheckWorker
                    OneTimeWorkRequest goalCheckWorkRequest = new OneTimeWorkRequest.Builder(GoalCheckWorker.class)
                            .build();
                    WorkManager.getInstance(context).enqueue(goalCheckWorkRequest);
                })
                .addOnFailureListener(e -> Toast.makeText(AddWorkoutActivity.this, "Failed to add workout", Toast.LENGTH_SHORT).show());
    }

    private void loadWorkouts() {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db.collection("users").document(uid).collection("workouts").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    workoutsList.clear();
                    workoutIds.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String sport = document.getString("sport");
                        String distanceReps = document.getString("distanceReps");
                        String date = document.getString("date");
                        String photoBase64 = document.getString("photoBase64");
                        String description = "Sport: " + sport + ", Distance/Reps: " + distanceReps;
                        workoutsList.add(new Workout(uid, sport, distanceReps, date, description, photoBase64));
                        workoutIds.add(document.getId());
                    }
                    workoutsAdapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(listViewWorkouts);
                })
                .addOnFailureListener(e -> Log.e("AddWorkoutActivity", "Error loading workouts", e));
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

    private void resetFields() {
        currentPhotoBase64 = null; // Reset the current photo after confirming
        imageViewPhotoPreview.setVisibility(View.GONE); // Hide the photo preview
        editTextDistanceReps.setText(""); // Clear the EditText
        textViewSelectedDate.setText("No date selected"); // Clear the date text
        selectedDate = null; // Clear the selected date
    }

    private void showImagePreview(String imageBase64) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ImagePreviewDialogFragment.newInstance(imageBase64).show(fragmentManager, "image_preview");
    }
}
