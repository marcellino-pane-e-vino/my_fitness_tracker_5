package com.example.my_fitness_tracker_5;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.text.MessageFormat;

public class PullUpCounterActivity extends BaseCounterActivity {

    // State variables for pullup detection
    private static final float PULL_UP_THRESHOLD = 1.5f;
    private static final int STATE_HANGING = 0;
    private static final int STATE_PULLING_UP = 1;
    private int pullUpState = STATE_HANGING;

    // Keys for saving instance state
    private static final String KEY_COUNT = "count";
    private static final String KEY_PULL_UP_STATE = "pullUpState";
    private static final String KEY_IS_COUNTING = "isCounting";

    @Override
    protected String getTitleText() {
        return "Pullup Counter";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore saved state if available
        if (savedInstanceState != null) {
            count = savedInstanceState.getInt(KEY_COUNT, 0);
            pullUpState = savedInstanceState.getInt(KEY_PULL_UP_STATE, STATE_HANGING);
            isCounting = savedInstanceState.getBoolean(KEY_IS_COUNTING, false);
            textCount.setText(MessageFormat.format("Pullups: {0}", count));

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
        textCount.setText(MessageFormat.format("Pullups: {0}", count));
        if (count == 0) {
            buttonStop.setEnabled(false);
        } else {
            saveWorkoutToFirestore("Pullup", "Pullup workout");
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

            float y = linearAcceleration[1]; // Use the Y axis for pullup detection

            switch (pullUpState) {
                case STATE_HANGING:
                    if (y < -PULL_UP_THRESHOLD) {
                        pullUpState = STATE_PULLING_UP;
                    }
                    break;
                case STATE_PULLING_UP:
                    if (y > PULL_UP_THRESHOLD) {
                        pullUpState = STATE_HANGING;
                        count++;
                        textCount.setText(MessageFormat.format("Pullups: {0}", count));
                    }
                    break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_COUNT, count);
        outState.putInt(KEY_PULL_UP_STATE, pullUpState);
        outState.putBoolean(KEY_IS_COUNTING, isCounting);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        count = savedInstanceState.getInt(KEY_COUNT, 0);
        pullUpState = savedInstanceState.getInt(KEY_PULL_UP_STATE, STATE_HANGING);
        isCounting = savedInstanceState.getBoolean(KEY_IS_COUNTING, false);
        textCount.setText(MessageFormat.format("Pullups: {0}", count));

        if (isCounting) {
            startCounting();
        }
    }
}
