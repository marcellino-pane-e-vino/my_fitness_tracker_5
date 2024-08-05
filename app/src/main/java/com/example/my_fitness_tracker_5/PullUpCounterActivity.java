package com.example.my_fitness_tracker_5;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.text.MessageFormat;

public class PullUpCounterActivity extends BaseCounterActivity {

    private Sensor accelerometer;

    // State variables for pullup detection
    private static final float PULL_UP_THRESHOLD = 1.5f;
    private static final int STATE_HANGING = 0;
    private static final int STATE_PULLING_UP = 1;
    private int pullUpState = STATE_HANGING;

    @Override
    protected String getTitleText() {
        return "Pullup Counter";
    }

    @Override
    protected void startCounting() {
        isCounting = true;
        count = 0;
        buttonStart.setEnabled(false);
        buttonStop.setEnabled(true);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
                        textCount.setText(MessageFormat.format("pullups: {0}", count));
                    }
                    break;
            }
        }
    }
}
