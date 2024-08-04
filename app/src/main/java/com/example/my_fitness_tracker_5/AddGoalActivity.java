package com.example.my_fitness_tracker_5;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddGoalActivity extends AppCompatActivity {

    private Spinner spinnerSport;
    private EditText editTextDistanceReps;
    private TextView textViewSelectedDate;
    private GoalAdapter goalsAdapter;
    private ArrayList<String> goalsList;

    private String selectedDate;
    private Context context;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        context = this;
        spinnerSport = findViewById(R.id.spinner_sport);
        editTextDistanceReps = findViewById(R.id.editText_distance_reps);
        Button buttonSelectDate = findViewById(R.id.button_select_date);
        Button buttonConfirmGoal = findViewById(R.id.button_confirm_goal);
        textViewSelectedDate = findViewById(R.id.textView_selected_date);
        RecyclerView recyclerViewGoals = findViewById(R.id.recyclerView_goals);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add a Goal");
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        goalsList = new ArrayList<>();
        goalsAdapter = new GoalAdapter(this, goalsList);
        recyclerViewGoals.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGoals.setAdapter(goalsAdapter);

        selectedDate = "";

        buttonSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year1, month1, dayOfMonth) -> {
                selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                textViewSelectedDate.setText("Selected Date: " + selectedDate);
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

            saveGoalToFirestore(sport, distanceReps, selectedDate);
        });

        loadUserGoals();
    }

    private void saveGoalToFirestore(String sport, String distanceReps, String date) {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Map<String, Object> goal = new HashMap<>();
        goal.put("uid", uid);
        goal.put("sport", sport);
        goal.put("distanceReps", distanceReps);
        goal.put("date", date);

        db.collection("users").document(uid).collection("goals").add(goal)
                .addOnSuccessListener(documentReference -> Toast.makeText(AddGoalActivity.this, "Goal added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AddGoalActivity.this, "Failed to add goal", Toast.LENGTH_SHORT).show());
    }

    private void loadUserGoals() {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db.collection("users").document(uid).collection("goals").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    goalsList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String sport = document.getString("sport");
                        String distanceReps = document.getString("distanceReps");
                        String date = document.getString("date");
                        String goal = "Sport: " + sport + ", Distance/Reps: " + distanceReps + ", Date: " + date;
                        goalsList.add(goal);
                    }
                    goalsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(AddGoalActivity.this, "Failed to load goals", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // This will navigate back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
