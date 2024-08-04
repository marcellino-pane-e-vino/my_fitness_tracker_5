package com.example.my_fitness_tracker_5;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        buttonStartSquats.setOnClickListener(v -> startCounting());
        buttonStopSquats.setOnClickListener(v -> stopCounting());
    }

    private void startCounting() {
        isCounting = true;
        squatCount = 0;
        lastY = 0;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopCounting() {
        isCounting = false;
        sensorManager.unregisterListener(this);
        textSquatCount.setText("Squats: " + squatCount);

        // Create a new Workout entry with current date and squat count
        String uid = mAuth.getCurrentUser().getUid();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String date = dateFormat.format(new Date());

        // Add description and photoBase64 if needed
        String description = "Squat workout";
        String photoBase64 = ""; // Add logic to capture or attach a photo if needed

        Workout workout = new Workout(uid, "Squats", String.valueOf(squatCount), date, description, photoBase64);

        db.collection("users").document(uid).collection("workouts").add(workout)
                .addOnSuccessListener(documentReference -> {
                    // Handle success
                })
                .addOnFailureListener(e -> {
                    // Handle failure
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
