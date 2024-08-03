package com.example.my_fitness_tracker_5;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ViewProgressActivity extends AppCompatActivity {

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String WORKOUTS_KEY = "workouts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_progress);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("View Progress");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        GraphView graphWeeklyWorkouts = findViewById(R.id.graph_weekly_workouts);

        Map<String, Integer> weeklyWorkoutCount = getWeeklyWorkoutCount();
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(getDataPoints(weeklyWorkoutCount));
        graphWeeklyWorkouts.addSeries(series);

        // Customize the graph (optional)
        graphWeeklyWorkouts.setTitle("Workouts per Week");
        graphWeeklyWorkouts.getGridLabelRenderer().setHorizontalAxisTitle("Week");
        graphWeeklyWorkouts.getGridLabelRenderer().setVerticalAxisTitle("Workouts");
    }

    private Map<String, Integer> getWeeklyWorkoutCount() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> workoutsSet = sharedPreferences.getStringSet(WORKOUTS_KEY, null);

        Map<String, Integer> weeklyWorkoutCount = new HashMap<>();
        if (workoutsSet != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-ww", Locale.getDefault());
            for (String workoutString : workoutsSet) {
                Workout workout = Workout.fromString(workoutString);
                String week = dateFormat.format(new Date());
                weeklyWorkoutCount.put(week, weeklyWorkoutCount.getOrDefault(week, 0) + 1);
            }
        }
        return weeklyWorkoutCount;
    }

    private DataPoint[] getDataPoints(Map<String, Integer> weeklyWorkoutCount) {
        DataPoint[] dataPoints = new DataPoint[weeklyWorkoutCount.size()];
        int index = 0;
        for (Map.Entry<String, Integer> entry : weeklyWorkoutCount.entrySet()) {
            dataPoints[index++] = new DataPoint(Integer.parseInt(entry.getKey().split("-")[1]), entry.getValue());
        }
        return dataPoints;
    }
}
