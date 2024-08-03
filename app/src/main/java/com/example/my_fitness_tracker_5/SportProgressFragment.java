package com.example.my_fitness_tracker_5;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class SportProgressFragment extends Fragment {

    private static final String ARG_SPORT = "sport";

    private String sport;

    public static SportProgressFragment newInstance(String sport) {
        SportProgressFragment fragment = new SportProgressFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SPORT, sport);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sport = getArguments().getString(ARG_SPORT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sport_progress, container, false);

        TextView textSportTitle = view.findViewById(R.id.text_sport_title);
        textSportTitle.setText(sport);

        GraphView graphSportWorkouts = view.findViewById(R.id.graph_sport_workouts);
        TextView textTotalWorkouts = view.findViewById(R.id.text_total_workouts);
        TextView textWeeklyWorkouts = view.findViewById(R.id.text_weekly_workouts);
        TextView textMonthlyWorkouts = view.findViewById(R.id.text_monthly_workouts);
        TextView textYearlyWorkouts = view.findViewById(R.id.text_yearly_workouts);

        Map<String, Integer> dailyWorkoutCount = getDailyWorkoutCountForCurrentWeek();
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(getDataPoints(dailyWorkoutCount));
        graphSportWorkouts.addSeries(series);

        // Customize the graph
        graphSportWorkouts.setTitle("Workouts This Week");
        graphSportWorkouts.getGridLabelRenderer().setHorizontalAxisTitle("Day");

        // Adjust viewport to show all seven days
        graphSportWorkouts.getViewport().setMinX(1);
        graphSportWorkouts.getViewport().setMaxX(7);
        graphSportWorkouts.getViewport().setXAxisBoundsManual(true);
        graphSportWorkouts.getGridLabelRenderer().setNumHorizontalLabels(7);

        // Adjust bar width and spacing
        series.setSpacing(5); // Adjust spacing between bars if needed
        series.setDataWidth(0.2); // Set the width of each bar

        // Ensure the graph fits within the screen
        graphSportWorkouts.getViewport().setScrollable(false);
        graphSportWorkouts.getViewport().setScalable(false);
        graphSportWorkouts.getViewport().setScrollableY(false);
        graphSportWorkouts.getViewport().setScalableY(false);

        // Custom label formatter to show day names
        graphSportWorkouts.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    String[] daysOfWeek = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
                    int dayIndex = (int) value - 1;
                    if (dayIndex >= 0 && dayIndex < daysOfWeek.length) {
                        return daysOfWeek[dayIndex];
                    }
                }
                return super.formatLabel(value, isValueX);
            }
        });

        // Set the number of vertical labels to integer values
        graphSportWorkouts.getGridLabelRenderer().setNumVerticalLabels(5); // Adjust this as needed

        // Display additional statistics
        displayStatistics(textTotalWorkouts, textWeeklyWorkouts, textMonthlyWorkouts, textYearlyWorkouts);

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void displayStatistics(TextView textTotalWorkouts, TextView textWeeklyWorkouts, TextView textMonthlyWorkouts, TextView textYearlyWorkouts) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(ViewProgressActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        Set<String> workoutsSet = sharedPreferences.getStringSet(ViewProgressActivity.WORKOUTS_KEY, null);

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
                if (workout != null && (sport.equals("General") || workout.getDescription().contains(sport))) {
                    try {
                        Date workoutDate = dateFormat.parse(workout.getDate());
                        assert workoutDate != null;
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

            textTotalWorkouts.setText("Total Workouts: " + totalWorkouts);
            textWeeklyWorkouts.setText("Workouts This Week: " + weeklyWorkouts);
            textMonthlyWorkouts.setText("Workouts This Month: " + monthlyWorkouts);
            textYearlyWorkouts.setText("Workouts This Year: " + yearlyWorkouts);
        }
    }

    private Map<String, Integer> getDailyWorkoutCountForCurrentWeek() {
        getActivity();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(ViewProgressActivity.SHARED_PREFS, Context.MODE_PRIVATE);
        Set<String> workoutsSet = sharedPreferences.getStringSet(ViewProgressActivity.WORKOUTS_KEY, null);

        Map<String, Integer> dailyWorkoutCount = new HashMap<>();
        if (workoutsSet != null) {
            Calendar calendar = Calendar.getInstance();
            int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
            int currentYear = calendar.get(Calendar.YEAR);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat dayFormat = new SimpleDateFormat("u", Locale.getDefault()); // Day of the week (1 = Monday, ..., 7 = Sunday)

            for (String workoutString : workoutsSet) {
                Workout workout = Workout.fromString(workoutString);
                if (workout != null && (sport.equals("General") || workout.getDescription().contains(sport))) {
                    try {
                        Date workoutDate = dateFormat.parse(workout.getDate());
                        assert workoutDate != null;
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
