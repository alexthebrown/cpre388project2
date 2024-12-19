package com.example.project2_subcompanion;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class RoomOptionsActivity extends AppCompatActivity {

    private ProgressBar attendanceProgressBar;
    private TextView attendanceTextView;
    private EditText capacityEditText;
    private Button updateCapacityButton;

    private FirebaseFirestore db;
    private DocumentReference attendanceRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_options); // Replace with your layout file name

        attendanceProgressBar = findViewById(R.id.attendanceProgressBar);
        attendanceTextView = findViewById(R.id.attendanceTextView);
        capacityEditText = findViewById(R.id.capacityEditText);
        updateCapacityButton = findViewById(R.id.updateCapacityButton);

        db = FirebaseFirestore.getInstance();
        attendanceRef = db.collection("eventAttendance").document("attendance");

        // Fetch initial attendance data
        fetchAttendanceData();

        updateCapacityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEventCapacity();
            }
        });
    }

    private void fetchAttendanceData() {
        attendanceRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        int filled = document.getLong("filled").intValue();
                        int capacity = document.getLong("capacity").intValue();

                        attendanceProgressBar.setProgress(filled);
                        attendanceProgressBar.setMax(capacity);
                        attendanceTextView.setText(filled + "/" + capacity);
                    } else {
                        // Handle case where document doesn't exist
                        Toast.makeText(RoomOptionsActivity.this, "Attendance document not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error
                    Toast.makeText(RoomOptionsActivity.this, "Failed to fetch attendance data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateEventCapacity() {
        String capacityString = capacityEditText.getText().toString();
        if (capacityString.isEmpty()) {
            Toast.makeText(this, "Please enter a capacity", Toast.LENGTH_SHORT).show();
            return;
        }

        int newCapacity = Integer.parseInt(capacityString);

        attendanceRef.update("capacity", newCapacity)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RoomOptionsActivity.this, "Capacity updated successfully", Toast.LENGTH_SHORT).show();
                        // You might want to refetch attendance data here to update the UI
                        fetchAttendanceData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RoomOptionsActivity.this, "Failed to update capacity", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}