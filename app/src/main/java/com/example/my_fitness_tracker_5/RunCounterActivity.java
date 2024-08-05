package com.example.my_fitness_tracker_5;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.text.MessageFormat;

public class RunCounterActivity extends BaseCounterActivity {

    private Sensor stepCounter;

    @Override
    protected String getTitleText() {
        return "Run Counter";
    }

    @Override
    protected void startCounting() {
        isCounting = true;
        count = 0;
        buttonStart.setEnabled(false);
        buttonStop.setEnabled(true);
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
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
}
