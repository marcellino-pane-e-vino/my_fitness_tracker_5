package com.example.my_fitness_tracker_5;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public abstract class BaseCounterActivity extends AppCompatActivity implements SensorEventListener {

    protected TextView textCount;
    protected Button buttonStart, buttonStop;
    protected SensorManager sensorManager;
    protected boolean isCounting = false;
    protected int count = 0;

    protected FirebaseFirestore db;
    protected FirebaseAuth mAuth;

    // Low-pass filter constants
    protected static final float ALPHA = 0.8f;
    protected final float[] gravity = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        textCount = findViewById(R.id.text_count);
        buttonStart = findViewById(R.id.button_start);
        buttonStop = findViewById(R.id.button_stop);
        buttonStop.setEnabled(false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getTitleText());
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        buttonStart.setOnClickListener(v -> startCounting());
        buttonStop.setOnClickListener(v -> stopCounting());
    }

    protected abstract String getTitleText();

    protected abstract void startCounting();

    protected abstract void stopCounting();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Navigate back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void saveWorkoutToFirestore(String workoutType, String description) {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String date = dateFormat.format(new Date());
        String photoBase64 = ""; // Add logic to capture or attach a photo if needed

        Workout workout = new Workout(uid, workoutType, String.valueOf(count), date, description, photoBase64);

        db.collection("users").document(uid).collection("workouts").add(workout)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Workout saved", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save workout", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
