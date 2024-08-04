package com.example.my_fitness_tracker_5;

public class Workout {
    private String uid;
    private String date;
    private String description;
    private String photoBase64;
    private String sport;
    private String distanceReps;

    // No-argument constructor
    public Workout() {
    }

    // Constructor with arguments
    public Workout(String uid, String sport, String distanceReps, String date, String description, String photoBase64) {
        this.uid = uid;
        this.date = date;
        this.description = description;
        this.photoBase64 = photoBase64;
        this.sport = sport;
        this.distanceReps = distanceReps;
    }

    // Getters and setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoBase64() {
        return photoBase64;
    }

    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public String getDistanceReps() {
        return distanceReps;
    }

    public void setDistanceReps(String distanceReps) {
        this.distanceReps = distanceReps;
    }

    @Override
    public String toString() {
        return "Workout{" +
                "uid='" + uid + '\'' +
                ", date='" + date + '\'' +
                ", description='" + description + '\'' +
                ", photoBase64='" + photoBase64 + '\'' +
                ", sport='" + sport + '\'' +
                ", distanceReps='" + distanceReps + '\'' +
                '}';
    }

    // Static method to parse Workout object from string if needed
    public static Workout fromString(String workoutString) {
        // Implement the method to parse workoutString and return a Workout object
        return new Workout();
    }
}
