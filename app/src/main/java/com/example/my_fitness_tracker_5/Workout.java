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

    // Getters and setters

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
        // Customize the format if needed
        return description + ";;;" + photoBase64 + ";;;" + date;
    }

    public static Workout fromString(String str) {
        String[] parts = str.split(";;;");
        if (parts.length == 3) {
            return new Workout(parts[0], parts[1], parts[2]);
        }
        return null;
    }
}
