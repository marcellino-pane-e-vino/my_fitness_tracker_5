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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class SquatCounterActivity extends AppCompatActivity implements SensorEventListener {

    private TextView textSquatCount;
    private Button buttonStartSquats, buttonStopSquats;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isCounting = false;
    private int squatCount = 0;
    private float lastY;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squat_counter);

        textSquatCount = findViewById(R.id.text_squat_count);
        buttonStartSquats = findViewById(R.id.button_start_squats);
        buttonStopSquats = findViewById(R.id.button_stop_squats);
        buttonStopSquats.setEnabled(false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Squat Counter");
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        buttonStartSquats.setOnClickListener(v -> startCounting());
        buttonStopSquats.setOnClickListener(v -> stopCounting());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Navigate back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startCounting() {
        isCounting = true;
        squatCount = 0;
        lastY = 0;
        buttonStartSquats.setEnabled(false);
        buttonStopSquats.setEnabled(true);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopCounting() {
        isCounting = false;
        sensorManager.unregisterListener(this);
        textSquatCount.setText("Squats: " + squatCount);
        if (squatCount == 0) {
            buttonStopSquats.setEnabled(false);
        } else {
            saveWorkoutToFirestore();
        }
    }

    private void saveWorkoutToFirestore() {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String date = dateFormat.format(new Date());
        String description = "Squat workout";
        String photoBase64 = ""; // Add logic to capture or attach a photo if needed

        Workout workout = new Workout(uid, "Squats", String.valueOf(squatCount), date, description, photoBase64);

        db.collection("users").document(uid).collection("workouts").add(workout)
                .addOnSuccessListener(documentReference -> {
                    // Handle success
                    Toast.makeText(SquatCounterActivity.this, "Workout saved", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SquatCounterActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Go back to the previous activity
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(SquatCounterActivity.this, "Failed to save workout", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isCounting) {
            float y = event.values[1];

            if (lastY == 0) {
                lastY = y;
                return;
            }

            if (y - lastY > 3.5) {
                squatCount++;
                textSquatCount.setText("Squats: " + squatCount);
            }

            lastY = y;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
