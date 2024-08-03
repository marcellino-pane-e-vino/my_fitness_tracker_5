package com.example.my_fitness_tracker_5;

import android.util.Base64;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Workout {
    private String description;
    private String photoBase64;
    private String date;

    public Workout(String description, String photoBase64, String date) {
        this.description = description;
        this.photoBase64 = photoBase64;
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoBase64() {
        return photoBase64;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return description + "," + (photoBase64 != null ? photoBase64 : "") + "," + date;
    }

    public static Workout fromString(String workoutString) {
        String[] parts = workoutString.split(",", 3);
        String description = parts[0];
        String photoBase64 = parts.length > 1 ? parts[1] : null;
        String date = parts.length > 2 ? parts[2] : null;
        return new Workout(description, photoBase64, date);
    }
}
