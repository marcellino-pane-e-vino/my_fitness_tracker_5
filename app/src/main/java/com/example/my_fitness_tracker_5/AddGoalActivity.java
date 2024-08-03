package com.example.my_fitness_tracker_5;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AddGoalActivity extends AppCompatActivity {

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String GOALS_KEY = "goals";

    private Spinner spinnerSport;
    private EditText editTextDistanceReps;
    private TextView textViewSelectedDate;
    private ListView listViewGoals;
    private GoalAdapter goalsAdapter;
    private ArrayList<String> goalsList;

    private String selectedDate;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        context = this;
        spinnerSport = findViewById(R.id.spinner_sport);
        editTextDistanceReps = findViewById(R.id.editText_distance_reps);
        Button buttonSelectDate = findViewById(R.id.button_select_date);
        Button buttonConfirmGoal = findViewById(R.id.button_confirm_goal);
        textViewSelectedDate = findViewById(R.id.textView_selected_date);
        listViewGoals = findViewById(R.id.listView_goals);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add a Goal");

        goalsList = new ArrayList<>();
        goalsAdapter = new GoalAdapter(this, goalsList);
        listViewGoals.setAdapter(goalsAdapter);

        selectedDate = "";

        loadGoals();

        buttonSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    textViewSelectedDate.setText("Selected Date: " + selectedDate);
                }
            }, year, month, day);
            datePickerDialog.show();
        });

        buttonConfirmGoal.setOnClickListener(v -> {
            String sport = spinnerSport.getSelectedItem().toString();
            String distanceReps = editTextDistanceReps.getText().toString();

            if (distanceReps.isEmpty() || selectedDate.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String goal = "Sport: " + sport + ", Distance/Reps: " + distanceReps + ", Date: " + selectedDate;
            goalsList.add(goal);
            goalsAdapter.notifyDataSetChanged();

            saveGoals();
            setListViewHeightBasedOnChildren(listViewGoals);

            scheduleNotification(goal, selectedDate);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // This will navigate back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveGoals() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> goalsSet = new HashSet<>(goalsList);
        editor.putStringSet(GOALS_KEY, goalsSet);
        editor.apply();
    }

    private void loadGoals() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Set<String> goalsSet = sharedPreferences.getStringSet(GOALS_KEY, new HashSet<>());
        goalsList.clear();
        goalsList.addAll(goalsSet);
        goalsAdapter.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(listViewGoals);
    }

    private void scheduleNotification(String goal, String date) {
        // Schedule the notification for the goal's date.
        // This can be implemented using AlarmManager or WorkManager.
        Toast.makeText(context, "Goal scheduled: " + goal, Toast.LENGTH_SHORT).show();
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        ArrayAdapter adapter = (ArrayAdapter) listView.getAdapter();
        if (adapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}