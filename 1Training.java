package com.example.fyp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Training extends AppCompatActivity {

    private LinearLayout formLayout;
    private ListView lvTrainingSessions;
    private Button btnAddSession, btnEditSession, btnDeleteSession, btnSaveSession;
    private EditText etSessionName, etSessionDate, etSessionDescription;
    private CheckBox cbCompleted; // Add CheckBox
    private List<TrainingSession> sessions;
    private TrainingSessionAdapter adapter;
    private DatabaseReference sessionsRef;
    private TrainingSession selectedSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        formLayout = findViewById(R.id.formLayout);
        lvTrainingSessions = findViewById(R.id.lvTrainingSessions);
        btnAddSession = findViewById(R.id.btnAddSession);
        btnEditSession = findViewById(R.id.btnEditSession);
        btnDeleteSession = findViewById(R.id.btnDeleteSession);
        btnSaveSession = findViewById(R.id.btnSaveSession);
        etSessionName = findViewById(R.id.etSessionName);
        etSessionDate = findViewById(R.id.etSessionDate);
        etSessionDescription = findViewById(R.id.etSessionDescription);
        cbCompleted = findViewById(R.id.cbCompleted); // Initialize CheckBox

        // Initialize Firebase
        sessionsRef = FirebaseDatabase.getInstance().getReference("training_sessions");

        // Initialize session list and adapter
        sessions = new ArrayList<>();
        adapter = new TrainingSessionAdapter(this, sessions);
        lvTrainingSessions.setAdapter(adapter);
        lvTrainingSessions.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Load data from Firebase
        sessionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sessions.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TrainingSession session = snapshot.getValue(TrainingSession.class);
                    sessions.add(session);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Training.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });

        lvTrainingSessions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Set the selected session
                selectedSession = sessions.get(position);
                lvTrainingSessions.setItemChecked(position, true);
            }
        });

        btnAddSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                formLayout.setVisibility(View.VISIBLE);
                clearForm();
                selectedSession = null; // Clear the selected session
            }
        });

        btnEditSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedSession != null) {
                    populateForm(selectedSession);
                    formLayout.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(Training.this, "Please select a session to edit.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDeleteSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedSession != null) {
                    sessionsRef.child(selectedSession.getName()).removeValue();
                } else {
                    Toast.makeText(Training.this, "Please select a session to delete.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSaveSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etSessionName.getText().toString();
                String date = etSessionDate.getText().toString();
                String description = etSessionDescription.getText().toString();
                boolean completed = cbCompleted.isChecked(); // Get the state of the CheckBox

                if (name.isEmpty() || date.isEmpty() || description.isEmpty()) {
                    Toast.makeText(Training.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                TrainingSession session = new TrainingSession(name, date, description, completed);
                if (selectedSession == null) {
                    // New session, add it
                    sessionsRef.child(name).setValue(session);
                } else if (!selectedSession.getName().equals(name)) {
                    // Renaming session: delete old, add new
                    sessionsRef.child(selectedSession.getName()).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            sessionsRef.child(name).setValue(session);
                        } else {
                            Toast.makeText(Training.this, "Failed to rename session.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // No change in name, just update
                    sessionsRef.child(name).setValue(session);
                }

                formLayout.setVisibility(View.GONE);
            }
        });
    }

    private void clearForm() {
        etSessionName.setText("");
        etSessionDate.setText("");
        etSessionDescription.setText("");
        cbCompleted.setChecked(false); // Clear CheckBox state
    }

    private void populateForm(TrainingSession session) {
        etSessionName.setText(session.getName());
        etSessionDate.setText(session.getDate());
        etSessionDescription.setText(session.getDescription());
        cbCompleted.setChecked(session.isCompleted()); // Set CheckBox state
    }
}
