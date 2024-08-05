package com.example.my_fitness_tracker_5;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.text.MessageFormat;

public class PushUpCounterActivity extends BaseCounterActivity {

    private Sensor proximitySensor;

    @Override
    protected String getTitleText() {
        return "Pushup Counter";
    }

    @Override
    protected void startCounting() {
        isCounting = true;
        count = 0;
        buttonStart.setEnabled(false);
        buttonStop.setEnabled(true);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void stopCounting() {
        isCounting = false;
        if (proximitySensor != null) {
            sensorManager.unregisterListener(this);
        }
        textCount.setText(MessageFormat.format("Pushups: {0}", count));
        if (count == 0) {
            buttonStop.setEnabled(false);
        } else {
            saveWorkoutToFirestore("Pushup", "Pushup workout");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isCounting && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] < proximitySensor.getMaximumRange()) {
                // Near
                count++;
                textCount.setText(MessageFormat.format("Pushups: {0}", count));
            }
        }
    }
}
