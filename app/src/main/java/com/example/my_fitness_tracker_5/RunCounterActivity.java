package com.example.my_fitness_tracker_5;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.text.MessageFormat;

public class RunCounterActivity extends BaseCounterActivity {

    // Keys for saving instance state
    private static final String KEY_COUNT = "count";
    private static final String KEY_IS_COUNTING = "isCounting";

    @Override
    protected String getTitleText() {
        return "Run Counter";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore saved state if available
        if (savedInstanceState != null) {
            count = savedInstanceState.getInt(KEY_COUNT, 0);
            isCounting = savedInstanceState.getBoolean(KEY_IS_COUNTING, false);
            textCount.setText(MessageFormat.format("Steps: {0}", count));

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
        Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void stopCounting() {
        isCounting = false;
        sensorManager.unregisterListener(this);
        textCount.setText(MessageFormat.format("Steps: {0}", count));
        if (count == 0) {
            buttonStop.setEnabled(false);
        } else {
            saveWorkoutToFirestore("Run", "Run workout");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isCounting && event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            count = (int) event.values[0];
            textCount.setText(MessageFormat.format("Steps: {0}", count));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_COUNT, count);
        outState.putBoolean(KEY_IS_COUNTING, isCounting);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        count = savedInstanceState.getInt(KEY_COUNT, 0);
        isCounting = savedInstanceState.getBoolean(KEY_IS_COUNTING, false);
        textCount.setText(MessageFormat.format("Steps: {0}", count));

        if (isCounting) {
            startCounting();
        }
    }
}
