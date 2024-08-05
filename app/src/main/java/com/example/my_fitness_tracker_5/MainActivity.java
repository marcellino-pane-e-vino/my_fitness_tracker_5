package com.example.my_fitness_tracker_5;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore.getInstance();

        // Check if the user is logged in
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("My Fitness Tracker");

        Button buttonAddGoal = findViewById(R.id.button_add_goal);
        Button buttonAddWorkout = findViewById(R.id.button_add_workout);
        Button buttonSquatCounter = findViewById(R.id.button_squat_counter);
        Button buttonPullUpCounter = findViewById(R.id.button_pull_up_counter);
        Button buttonPushUpCounter = findViewById(R.id.button_push_up_counter);
        Button buttonWalkCounter = findViewById(R.id.button_walk_counter);
        Button buttonRunCounter = findViewById(R.id.button_run_counter);
        Button buttonViewProgress = findViewById(R.id.button_view_progress);
        Button buttonProfile = findViewById(R.id.button_profile);

        buttonAddGoal.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddGoalActivity.class)));
        buttonAddWorkout.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddWorkoutActivity.class)));
        buttonSquatCounter.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SquatCounterActivity.class)));
        buttonPullUpCounter.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PullUpCounterActivity.class)));
        buttonPushUpCounter.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PushUpCounterActivity.class)));
        buttonWalkCounter.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WalkCounterActivity.class)));
        buttonRunCounter.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RunCounterActivity.class)));
        buttonViewProgress.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ViewProgressActivity.class)));
        buttonProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        // Schedule the GoalCheckWorker to run periodically
        PeriodicWorkRequest goalCheckWorkRequest = new PeriodicWorkRequest.Builder(GoalCheckWorker.class, 24, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(this).enqueue(goalCheckWorkRequest);
    }
}
