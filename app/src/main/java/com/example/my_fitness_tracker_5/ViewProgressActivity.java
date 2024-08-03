package com.example.my_fitness_tracker_5;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.DefaultLabelFormatter;

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
    private static final String[] DAYS_OF_WEEK = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};

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

        // Adjust viewport to show all seven days
        graphWeeklyWorkouts.getViewport().setMinX(1);
        graphWeeklyWorkouts.getViewport().setMaxX(7);
        graphWeeklyWorkouts.getViewport().setXAxisBoundsManual(true);
        graphWeeklyWorkouts.getGridLabelRenderer().setNumHorizontalLabels(7);

        // Adjust bar width and spacing
        series.setSpacing(5); // Adjust spacing between bars if needed
        series.setDataWidth(0.2); // Set the width of each bar

        // Ensure the graph fits within the screen
        graphWeeklyWorkouts.getViewport().setScrollable(false);
        graphWeeklyWorkouts.getViewport().setScalable(false);
        graphWeeklyWorkouts.getViewport().setScrollableY(false);
        graphWeeklyWorkouts.getViewport().setScalableY(false);

        // Custom label formatter to show day names
        graphWeeklyWorkouts.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    int dayIndex = (int) value - 1;
                    if (dayIndex >= 0 && dayIndex < DAYS_OF_WEEK.length) {
                        return DAYS_OF_WEEK[dayIndex];
                    }
                }
                return super.formatLabel(value, isValueX);
            }
        });

        // Set the number of vertical labels to integer values
        graphWeeklyWorkouts.getGridLabelRenderer().setNumVerticalLabels(5); // Adjust this as needed

        // Display additional statistics
        displayStatistics();
    }

    private void displayStatistics() {
        TextView textTotalWorkouts = findViewById(R.id.text_total_workouts);
        TextView textWeeklyWorkouts = findViewById(R.id.text_weekly_workouts);
        TextView textMonthlyWorkouts = findViewById(R.id.text_monthly_workouts);
        TextView textYearlyWorkouts = findViewById(R.id.text_yearly_workouts);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> workoutsSet = sharedPreferences.getStringSet(WORKOUTS_KEY, null);

        if (workoutsSet != null) {
            Calendar calendar = Calendar.getInstance();
            int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);
            int currentYear = calendar.get(Calendar.YEAR);

            int totalWorkouts = 0;
            int weeklyWorkouts = 0;
            int monthlyWorkouts = 0;
            int yearlyWorkouts = 0;

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            for (String workoutString : workoutsSet) {
                Workout workout = Workout.fromString(workoutString);
                if (workout != null) {
                    try {
                        Date workoutDate = dateFormat.parse(workout.getDate());
                        calendar.setTime(workoutDate);

                        totalWorkouts++;

                        int workoutWeek = calendar.get(Calendar.WEEK_OF_YEAR);
                        int workoutMonth = calendar.get(Calendar.MONTH);
                        int workoutYear = calendar.get(Calendar.YEAR);

                        if (workoutWeek == currentWeek && workoutYear == currentYear) {
                            weeklyWorkouts++;
                        }

                        if (workoutMonth == currentMonth && workoutYear == currentYear) {
                            monthlyWorkouts++;
                        }

                        if (workoutYear == currentYear) {
                            yearlyWorkouts++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            textWeeklyWorkouts.setText("Workouts This Week: " + weeklyWorkouts);
            textMonthlyWorkouts.setText("Workouts This Month: " + monthlyWorkouts);
            textYearlyWorkouts.setText("Workouts This Year: " + yearlyWorkouts);
            textTotalWorkouts.setText("Total Workouts: " + totalWorkouts);
        }
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
