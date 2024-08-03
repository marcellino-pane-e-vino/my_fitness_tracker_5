package com.example.my_fitness_tracker_5;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.Series;

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

        Map<String, Integer> dailyWorkoutCount = getDailyWorkoutCountForCurrentWeek();
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(getDataPoints(dailyWorkoutCount));
        graphWeeklyWorkouts.addSeries(series);

        // Customize the graph
        graphWeeklyWorkouts.setTitle("Workouts This Week");
        graphWeeklyWorkouts.getGridLabelRenderer().setHorizontalAxisTitle("Day");
        graphWeeklyWorkouts.getGridLabelRenderer().setVerticalAxisTitle("Workouts");
        graphWeeklyWorkouts.getViewport().setMinX(1);
        graphWeeklyWorkouts.getViewport().setMaxX(7);
        graphWeeklyWorkouts.getViewport().setXAxisBoundsManual(true);
        graphWeeklyWorkouts.getGridLabelRenderer().setNumHorizontalLabels(7);

        // Adjust bar width
        series.setSpacing(10); // Set spacing between bars
        series.setDataWidth(0.8); // Set the width of each bar

        // Ensure the graph fits within the screen
        graphWeeklyWorkouts.getViewport().setScrollable(true);
        graphWeeklyWorkouts.getViewport().setScalableY(true);
    }

    private Map<String, Integer> getDailyWorkoutCountForCurrentWeek() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> workoutsSet = sharedPreferences.getStringSet(WORKOUTS_KEY, null);

        Map<String, Integer> dailyWorkoutCount = new HashMap<>();
        if (workoutsSet != null) {
            Calendar calendar = Calendar.getInstance();
            int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
            int currentYear = calendar.get(Calendar.YEAR);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat dayFormat = new SimpleDateFormat("u", Locale.getDefault()); // Day of the week (1 = Monday, ..., 7 = Sunday)

            for (String workoutString : workoutsSet) {
                Workout workout = Workout.fromString(workoutString);
                if (workout != null) {
                    try {
                        Date workoutDate = dateFormat.parse(workout.getDate());
                        calendar.setTime(workoutDate);
                        int workoutWeek = calendar.get(Calendar.WEEK_OF_YEAR);
                        int workoutYear = calendar.get(Calendar.YEAR);

                        if (workoutWeek == currentWeek && workoutYear == currentYear) {
                            String day = dayFormat.format(workoutDate);
                            dailyWorkoutCount.put(day, dailyWorkoutCount.getOrDefault(day, 0) + 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return dailyWorkoutCount;
    }

    private DataPoint[] getDataPoints(Map<String, Integer> dailyWorkoutCount) {
        DataPoint[] dataPoints = new DataPoint[7];
        for (int i = 1; i <= 7; i++) {
            String day = String.valueOf(i);
            int count = dailyWorkoutCount.getOrDefault(day, 0);
            dataPoints[i - 1] = new DataPoint(i, count);
        }
        return dataPoints;
    }
}
