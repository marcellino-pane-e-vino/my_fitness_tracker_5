package com.example.my_fitness_tracker_5;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ViewProgressActivity extends AppCompatActivity {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String WORKOUTS_KEY = "workouts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_progress);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("View Progress");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ViewPager2 viewPager = findViewById(R.id.view_pager);

        List<String> sports = Arrays.asList("General", "Run", "Walk", "Squat", "Pullup", "Pushup");
        SportPagerAdapter adapter = new SportPagerAdapter(this, sports);
        viewPager.setAdapter(adapter);
    }
}
