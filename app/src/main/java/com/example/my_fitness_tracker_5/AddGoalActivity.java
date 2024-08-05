package com.example.my_fitness_tracker_5;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
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

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AddGoalActivity extends AppCompatActivity {

    private Spinner spinnerSport;
    private EditText editTextDistanceReps;
    private TextView textViewSelectedDate;
    private GoalAdapter goalsAdapter;
    private ArrayList<String> goalsList;
    private ArrayList<String> goalIds;

    private String selectedStartDate;
    private String selectedExpiryDate;
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
        new GoalCheckUtil(context);
        spinnerSport = findViewById(R.id.spinner_sport);
        editTextDistanceReps = findViewById(R.id.editText_distance_reps);
        // start date is always current date
        Button buttonSelectExpiryDate = findViewById(R.id.button_select_date);
        Button buttonConfirmGoal = findViewById(R.id.button_confirm_goal);
        textViewSelectedDate = findViewById(R.id.textView_selected_date);
        RecyclerView recyclerViewGoals = findViewById(R.id.recyclerView_goals);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Add a Goal");
        Objects.requireNonNull(toolbar.getNavigationIcon()).setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);

        goalsList = new ArrayList<>();
        goalIds = new ArrayList<>();
        goalsAdapter = new GoalAdapter(this, goalsList, goalIds);
        recyclerViewGoals.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGoals.setAdapter(goalsAdapter);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedStartDate = sdf.format(Calendar.getInstance().getTime());
        selectedExpiryDate = "";

        buttonSelectExpiryDate.setOnClickListener(v -> showDatePickerDialog());

        buttonConfirmGoal.setOnClickListener(v -> {
            String sport = spinnerSport.getSelectedItem().toString();
            String distanceReps = editTextDistanceReps.getText().toString();

            if (distanceReps.isEmpty() || selectedStartDate.isEmpty() || selectedExpiryDate.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String goal = "Sport: " + sport + ", Distance/Reps: " + distanceReps + ", Start Date: " + selectedStartDate + ", Expiry Date: " + selectedExpiryDate;

            goalsList.add(goal);
            goalIds.add(null); // Placeholder for the ID that will be added later
            goalsAdapter.notifyDataSetChanged();

            saveGoalToFirestore(sport, distanceReps, selectedStartDate, selectedExpiryDate);
        });

        loadUserGoals();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year1, month1, dayOfMonth) -> {
            selectedExpiryDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            textViewSelectedDate.setText(MessageFormat.format("Selected Expiry Date: {0}", selectedExpiryDate));
        }, year, month, day);
        datePickerDialog.show();
    }

    private void saveGoalToFirestore(String sport, String distanceReps, String startDate, String expiryDate) {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Map<String, Object> goal = new HashMap<>();
        goal.put("uid", uid);
        goal.put("sport", sport);
        goal.put("distanceReps", distanceReps);
        goal.put("startDate", startDate);
        goal.put("expiryDate", expiryDate);

        db.collection("users").document(uid).collection("goals").add(goal)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddGoalActivity.this, "Goal added", Toast.LENGTH_SHORT).show();
                    int index = goalsList.size() - 1;
                    goalIds.set(index, documentReference.getId());
                })
                .addOnFailureListener(e -> Toast.makeText(AddGoalActivity.this, "Failed to add goal", Toast.LENGTH_SHORT).show());
    }

    private void loadUserGoals() {
        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        db.collection("users").document(uid).collection("goals").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    goalsList.clear();
                    goalIds.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String sport = document.getString("sport");
                        String distanceReps = document.getString("distanceReps");
                        String startDate = document.getString("startDate");
                        String expiryDate = document.getString("expiryDate");
                        String goal = "Sport: " + sport + ", Distance/Reps: " + distanceReps + ", Start Date: " + startDate + ", Expiry Date: " + expiryDate;
                        goalsList.add(goal);
                        goalIds.add(document.getId());
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
