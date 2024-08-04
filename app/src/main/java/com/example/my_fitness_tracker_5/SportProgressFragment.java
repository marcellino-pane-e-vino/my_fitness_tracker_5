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
        TextView textTotalWorkouts = view.findViewById(R.id.text_total_workouts);
        TextView textWeeklyWorkouts = view.findViewById(R.id.text_weekly_workouts);
        TextView textMonthlyWorkouts = view.findViewById(R.id.text_monthly_workouts);
        TextView textYearlyWorkouts = view.findViewById(R.id.text_yearly_workouts);

        // Fetch data from Firestore and display it
        fetchWorkoutDataAndDisplay(graphSportWorkouts, textTotalWorkouts, textWeeklyWorkouts, textMonthlyWorkouts, textYearlyWorkouts);

        return view;
    }

    private void fetchWorkoutDataAndDisplay(GraphView graphSportWorkouts, TextView textTotalWorkouts, TextView textWeeklyWorkouts, TextView textMonthlyWorkouts, TextView textYearlyWorkouts) {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db.collection("users").document(uid).collection("workouts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Integer> dailyWorkoutCount = new HashMap<>();
                        int totalWorkouts = 0;
                        int weeklyWorkouts = 0;
                        int monthlyWorkouts = 0;
                        int yearlyWorkouts = 0;

                        Calendar calendar = Calendar.getInstance();
                        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
                        int currentMonth = calendar.get(Calendar.MONTH);
                        int currentYear = calendar.get(Calendar.YEAR);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        SimpleDateFormat dayFormat = new SimpleDateFormat("u", Locale.getDefault()); // Day of the week (1 = Monday, ..., 7 = Sunday)

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Workout workout = document.toObject(Workout.class);
                            Log.d("SportProgressFragment", "Fetched workout: " + workout.toString());
                            if (workout != null && (sport.equals("General") || (workout.getSport() != null && workout.getSport().equalsIgnoreCase(sport)))) {
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

                                    String day = dayFormat.format(workoutDate);
                                    dailyWorkoutCount.put(day, dailyWorkoutCount.getOrDefault(day, 0) + 1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        // Update graph and statistics
                        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(getDataPoints(dailyWorkoutCount));
                        graphSportWorkouts.addSeries(series);

                        graphSportWorkouts.setTitle("Workouts This Week");
                        graphSportWorkouts.getGridLabelRenderer().setHorizontalAxisTitle("Day");
                        graphSportWorkouts.getViewport().setMinX(1);
                        graphSportWorkouts.getViewport().setMaxX(7);
                        graphSportWorkouts.getViewport().setXAxisBoundsManual(true);
                        graphSportWorkouts.getGridLabelRenderer().setNumHorizontalLabels(7);
                        series.setSpacing(5); // Adjust spacing between bars if needed
                        series.setDataWidth(0.2); // Set the width of each bar
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

                        textTotalWorkouts.setText("Total Workouts: " + totalWorkouts);
                        textWeeklyWorkouts.setText("Workouts This Week: " + weeklyWorkouts);
                        textMonthlyWorkouts.setText("Workouts This Month: " + monthlyWorkouts);
                        textYearlyWorkouts.setText("Workouts This Year: " + yearlyWorkouts);
                    } else {
                        Log.e("SportProgressFragment", "Error fetching workouts: ", task.getException());
                    }
                });
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
