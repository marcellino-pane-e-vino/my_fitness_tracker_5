package com.example.my_fitness_tracker_5;

import android.util.Base64;

public class Workout {
    private String description;
    private String photoBase64;

    public Workout(String description, String photoBase64) {
        this.description = description;
        this.photoBase64 = photoBase64;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoBase64() {
        return photoBase64;
    }

    @Override
    public String toString() {
        return description + "," + (photoBase64 != null ? photoBase64 : "");
    }

    public static Workout fromString(String workoutString) {
        String[] parts = workoutString.split(",", 2);
        String description = parts[0];
        String photoBase64 = parts.length > 1 ? parts[1] : null;
        return new Workout(description, photoBase64);
    }
}
