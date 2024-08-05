package com.example.my_fitness_tracker_5;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SportProgressFragment extends Fragment {

    private static final String ARG_SPORT = "sport";
    private String sport;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sport_progress, container, false);

        TextView textSportTitle = view.findViewById(R.id.text_sport_title);
        textSportTitle.setText(sport);

        GraphView graphSportWorkouts = view.findViewById(R.id.graph_sport_workouts);
        GraphView graphDistanceReps = view.findViewById(R.id.graph_distance_reps);
        TextView textTotalSportWorkouts = view.findViewById(R.id.text_total_sport_workouts);
        TextView textWeeklySportWorkouts = view.findViewById(R.id.text_weekly_sport_workouts);
        TextView textMonthlySportWorkouts = view.findViewById(R.id.text_monthly_sport_workouts);
        TextView textYearlySportWorkouts = view.findViewById(R.id.text_yearly_sport_workouts);
        TextView textTotalDistanceReps = view.findViewById(R.id.text_total_distance_reps);
        TextView textWeeklyDistanceReps = view.findViewById(R.id.text_weekly_distance_reps);
        TextView textMonthlyDistanceReps = view.findViewById(R.id.text_monthly_distance_reps);
        TextView textYearlyDistanceReps = view.findViewById(R.id.text_yearly_distance_reps);

        fetchWorkoutDataAndDisplay(graphSportWorkouts, graphDistanceReps, textTotalSportWorkouts, textWeeklySportWorkouts, textMonthlySportWorkouts, textYearlySportWorkouts,
                textTotalDistanceReps, textWeeklyDistanceReps, textMonthlyDistanceReps, textYearlyDistanceReps);

        return view;
    }

    private void fetchWorkoutDataAndDisplay(GraphView graphSportWorkouts, GraphView graphDistanceReps, TextView textTotalSportWorkouts,
                                            TextView textWeeklySportWorkouts, TextView textMonthlySportWorkouts, TextView textYearlySportWorkouts,
                                            TextView textTotalDistanceReps, TextView textWeeklyDistanceReps, TextView textMonthlyDistanceReps, TextView textYearlyDistanceReps) {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db.collection("users").document(uid).collection("workouts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Integer> dailyWorkoutCount = new HashMap<>();
                        Map<String, Double> dailyDistanceReps = new HashMap<>();
                        int totalSportWorkouts = 0;
                        int weeklySportWorkouts = 0;
                        int monthlySportWorkouts = 0;
                        int yearlySportWorkouts = 0;
                        double totalDistanceReps = 0;
                        double weeklyDistanceReps = 0;
                        double monthlyDistanceReps = 0;
                        double yearlyDistanceReps = 0;

                        Calendar calendar = Calendar.getInstance();
                        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
                        int currentMonth = calendar.get(Calendar.MONTH);
                        int currentYear = calendar.get(Calendar.YEAR);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        SimpleDateFormat dayFormat = new SimpleDateFormat("u", Locale.getDefault()); // Day of the week (1 = Monday, ..., 7 = Sunday)

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Workout workout = document.toObject(Workout.class);
                            Log.d("SportProgressFragment", "Fetched workout: " + workout);
                            if (sport.equalsIgnoreCase("General") || workout.getSport() != null && workout.getSport().equalsIgnoreCase(sport)) {
                                try {
                                    Date workoutDate = dateFormat.parse(workout.getDate());
                                    assert workoutDate != null;
                                    calendar.setTime(workoutDate);

                                    totalSportWorkouts++;

                                    int workoutWeek = calendar.get(Calendar.WEEK_OF_YEAR);
                                    int workoutMonth = calendar.get(Calendar.MONTH);
                                    int workoutYear = calendar.get(Calendar.YEAR);

                                    if (workoutWeek == currentWeek && workoutYear == currentYear) {
                                        weeklySportWorkouts++;
                                    }

                                    if (workoutMonth == currentMonth && workoutYear == currentYear) {
                                        monthlySportWorkouts++;
                                    }

                                    if (workoutYear == currentYear) {
                                        yearlySportWorkouts++;
                                    }

                                    double distanceReps = Double.parseDouble(workout.getDistanceReps());
                                    totalDistanceReps += distanceReps;

                                    if (workoutWeek == currentWeek && workoutYear == currentYear) {
                                        weeklyDistanceReps += distanceReps;
                                        String day = dayFormat.format(workoutDate);
                                        Integer countValue = dailyWorkoutCount.get(day);
                                        int count = (countValue != null) ? countValue : 0;
                                        dailyWorkoutCount.put(day, count + 1);

                                        Double distanceRepsValue = dailyDistanceReps.get(day);
                                        double distanceRepsCount = (distanceRepsValue != null) ? distanceRepsValue : 0;
                                        dailyDistanceReps.put(day, distanceRepsCount + distanceReps);
                                    }

                                    if (workoutMonth == currentMonth && workoutYear == currentYear) {
                                        monthlyDistanceReps += distanceReps;
                                    }

                                    if (workoutYear == currentYear) {
                                        yearlyDistanceReps += distanceReps;
                                    }
                                } catch (Exception e) {
                                    Log.e("SportProgressFragment", "Error parsing workout date: ", e);
                                }
                            }
                        }

                        // Update graphs and statistics
                        BarGraphSeries<DataPoint> workoutSeries = new BarGraphSeries<>(getDataPoints(dailyWorkoutCount));
                        graphSportWorkouts.addSeries(workoutSeries);

                        graphSportWorkouts.setTitle("Workouts This Week");
                        graphSportWorkouts.getGridLabelRenderer().setHorizontalAxisTitle("Day");
                        graphSportWorkouts.getViewport().setMinX(1);
                        graphSportWorkouts.getViewport().setMaxX(7);
                        graphSportWorkouts.getViewport().setXAxisBoundsManual(true);
                        graphSportWorkouts.getGridLabelRenderer().setNumHorizontalLabels(7);
                        workoutSeries.setSpacing(5); // Adjust spacing between bars if needed
                        workoutSeries.setDataWidth(0.2); // Set the width of each bar
                        graphSportWorkouts.getViewport().setScrollable(false);
                        graphSportWorkouts.getViewport().setScalable(false);
                        graphSportWorkouts.getViewport().setScrollableY(false);
                        graphSportWorkouts.getViewport().setScalableY(false);

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

                        BarGraphSeries<DataPoint> distanceRepsSeries = new BarGraphSeries<>(getDistanceRepsDataPoints(dailyDistanceReps));
                        graphDistanceReps.addSeries(distanceRepsSeries);

                        graphDistanceReps.setTitle("Distance/Reps This Week");
                        graphDistanceReps.getGridLabelRenderer().setHorizontalAxisTitle("Day");
                        graphDistanceReps.getViewport().setMinX(1);
                        graphDistanceReps.getViewport().setMaxX(7);
                        graphDistanceReps.getViewport().setXAxisBoundsManual(true);
                        graphDistanceReps.getGridLabelRenderer().setNumHorizontalLabels(7);
                        distanceRepsSeries.setSpacing(5); // Adjust spacing between bars if needed
                        distanceRepsSeries.setDataWidth(0.2); // Set the width of each bar
                        graphDistanceReps.getViewport().setScrollable(false);
                        graphDistanceReps.getViewport().setScalable(false);
                        graphDistanceReps.getViewport().setScrollableY(false);
                        graphDistanceReps.getViewport().setScalableY(false);

                        graphDistanceReps.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
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

                        textTotalSportWorkouts.setText(MessageFormat.format("Total {0} Workouts: {1}", sport, totalSportWorkouts));
                        textWeeklySportWorkouts.setText(MessageFormat.format("{0} Workouts This Week: {1}", sport, weeklySportWorkouts));
                        textMonthlySportWorkouts.setText(MessageFormat.format("{0} Workouts This Month: {1}", sport, monthlySportWorkouts));
                        textYearlySportWorkouts.setText(MessageFormat.format("{0} Workouts This Year: {1}", sport, yearlySportWorkouts));

                        if (!sport.equalsIgnoreCase("General")) {
                            textTotalDistanceReps.setVisibility(View.VISIBLE);
                            textWeeklyDistanceReps.setVisibility(View.VISIBLE);
                            textMonthlyDistanceReps.setVisibility(View.VISIBLE);
                            textYearlyDistanceReps.setVisibility(View.VISIBLE);

                            textTotalDistanceReps.setText(MessageFormat.format("Total {0} Distance/Reps: {1}", sport, totalDistanceReps));
                            textWeeklyDistanceReps.setText(MessageFormat.format("{0} Distance/Reps This Week: {1}", sport, weeklyDistanceReps));
                            textMonthlyDistanceReps.setText(MessageFormat.format("{0} Distance/Reps This Month: {1}", sport, monthlyDistanceReps));
                            textYearlyDistanceReps.setText(MessageFormat.format("{0} Distance/Reps This Year: {1}", sport, yearlyDistanceReps));

                        } else {
                            textTotalDistanceReps.setVisibility(View.GONE);
                            textWeeklyDistanceReps.setVisibility(View.GONE);
                            textMonthlyDistanceReps.setVisibility(View.GONE);
                            textYearlyDistanceReps.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e("SportProgressFragment", "Error fetching workouts: ", task.getException());
                    }
                });
    }

    private DataPoint[] getDataPoints(Map<String, Integer> dailyWorkoutCount) {
        DataPoint[] dataPoints = new DataPoint[7];
        for (int i = 1; i <= 7; i++) {
            String day = String.valueOf(i);
            Integer countValue = dailyWorkoutCount.get(day);
            int count = (countValue != null) ? countValue : 0;
            dataPoints[i - 1] = new DataPoint(i, count);
        }
        return dataPoints;
    }

    private DataPoint[] getDistanceRepsDataPoints(Map<String, Double> dailyDistanceReps) {
        DataPoint[] dataPoints = new DataPoint[7];
        for (int i = 1; i <= 7; i++) {
            String day = String.valueOf(i);
            Double countValue = dailyDistanceReps.get(day);
            double count = (countValue != null) ? countValue : 0;
            dataPoints[i - 1] = new DataPoint(i, count);
        }
        return dataPoints;
    }
}
