package com.example.my_fitness_tracker_5;

public class Workout {

    private String description;
    private String photo;

    public Workout(String description, String photo) {
        this.description = description;
        this.photo = photo;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoto() {
        return photo;
    }

    @Override
    public String toString() {
        return description + ";" + photo;
    }

    public static Workout fromString(String workoutString) {
        String[] parts = workoutString.split(";");
        return new Workout(parts[0], parts[1]);
    }
}
