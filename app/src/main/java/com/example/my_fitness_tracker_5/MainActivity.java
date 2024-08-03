package com.example.my_fitness_tracker_5;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("My Fitness Tracker");

        Button buttonAddGoal = findViewById(R.id.button_add_goal);
        Button buttonAddWorkout = findViewById(R.id.button_add_workout);
        Button buttonViewProgress = findViewById(R.id.button_view_progress);
        Button buttonProfile = findViewById(R.id.button_profile);

        buttonAddGoal.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddGoalActivity.class)));

        buttonAddWorkout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddWorkoutActivity.class)));

        buttonViewProgress.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ViewProgressActivity.class)));

//        buttonProfile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
//            }
//        });
    }
}
