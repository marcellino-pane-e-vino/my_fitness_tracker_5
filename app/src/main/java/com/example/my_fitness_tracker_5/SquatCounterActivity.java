package com.example.my_fitness_tracker_5;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.text.MessageFormat;

public class SquatCounterActivity extends BaseCounterActivity {

    // State variables for squat detection
    private static final float SQUAT_THRESHOLD = 1.5f;
    private static final int STATE_STANDING = 0;
    private static final int STATE_SQUATTING = 1;
    private int squatState = STATE_STANDING;

    // Keys for saving instance state
    private static final String KEY_COUNT = "count";
    private static final String KEY_SQUAT_STATE = "squatState";
    private static final String KEY_IS_COUNTING = "isCounting";

    @Override
    protected String getTitleText() {
        return "Squat Counter";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore saved state if available
        if (savedInstanceState != null) {
            count = savedInstanceState.getInt(KEY_COUNT, 0);
            squatState = savedInstanceState.getInt(KEY_SQUAT_STATE, STATE_STANDING);
            isCounting = savedInstanceState.getBoolean(KEY_IS_COUNTING, false);
            textCount.setText(MessageFormat.format("Squats: {0}", count));

            if (isCounting) {
                startCounting();
            }
        }
    }

    @Override
    protected void startCounting() {
        isCounting = true;
        buttonStart.setEnabled(false);
        buttonStop.setEnabled(true);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void stopCounting() {
        isCounting = false;
        sensorManager.unregisterListener(this);
        textCount.setText(MessageFormat.format("Squats: {0}", count));
        if (count == 0) {
            buttonStop.setEnabled(false);
        } else {
            saveWorkoutToFirestore("Squat", "Squat workout");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isCounting) {
            float[] linearAcceleration = new float[3];

            // Apply low-pass filter to isolate gravity
            for (int i = 0; i < 3; i++) {
                gravity[i] = ALPHA * gravity[i] + (1 - ALPHA) * event.values[i];
                linearAcceleration[i] = event.values[i] - gravity[i];
            }

            float y = linearAcceleration[1]; // Use the Y axis for squat detection

            switch (squatState) {
                case STATE_STANDING:
                    if (y < -SQUAT_THRESHOLD) {
                        squatState = STATE_SQUATTING;
                    }
                    break;
                case STATE_SQUATTING:
                    if (y > SQUAT_THRESHOLD) {
                        squatState = STATE_STANDING;
                        count++;
                        textCount.setText(MessageFormat.format("Squats: {0}", count));
                    }
                    break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_COUNT, count);
        outState.putInt(KEY_SQUAT_STATE, squatState);
        outState.putBoolean(KEY_IS_COUNTING, isCounting);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        count = savedInstanceState.getInt(KEY_COUNT, 0);
        squatState = savedInstanceState.getInt(KEY_SQUAT_STATE, STATE_STANDING);
        isCounting = savedInstanceState.getBoolean(KEY_IS_COUNTING, false);
        textCount.setText(MessageFormat.format("Squats: {0}", count));

        if (isCounting) {
            startCounting();
        }
    }
}
