package com.example.project2_subcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.gcm.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
/**
 * @author Alex Brown
 * Menu for execs to choose what they need to do in terms of check in stuff
 */
public class ScanInChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_in_choice); // Replace with your layout file name

        Button checkInGuestsButton = findViewById(R.id.checkInGuests);
        Button roomOptionsButton = findViewById(R.id.roomOptions);
        Button checkOutGuestsButton = findViewById(R.id.checkOutGuests);
        Button volunteersCheckinButton = findViewById(R.id.volunteersCheckinButton);

        checkInGuestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScanInChoiceActivity.this, TagReadActivity.class)); // Replace with your activity class
            }
        });

        roomOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScanInChoiceActivity.this, RoomOptionsActivity.class)); // Replace with your activity class
            }
        });

//

        checkOutGuestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                WriteBatch batch = db.batch();
                DocumentReference attendanceRef = db.collection("eventAttendance").document("attendance");

                db.collection("users").get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    batch.update(document.getReference(), "checkedInAD", false);
                                }

                                // Update "filled" to 0 in"attendance" document
                                batch.update(attendanceRef, "filled", 0); // This line was misplaced before

                                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ScanInChoiceActivity.this, "Successfully checked out all guests and reset attendance", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ScanInChoiceActivity.this, "Failed to check out all guests or reset attendance", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle the error
                                Toast.makeText(ScanInChoiceActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        volunteersCheckinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScanInChoiceActivity.this, VolunteersCheckInActivity.class)); // Replace with your activity class
            }
        });
    }
}