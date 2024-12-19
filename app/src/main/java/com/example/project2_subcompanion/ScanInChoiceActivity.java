package com.example.project2_subcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

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

        checkOutGuestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                WriteBatch batch = db.batch();

                db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                batch.update(document.getReference(), "checkedInAD", false);
                            }
                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ScanInChoiceActivity.this, "Successfully checked out all guests", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ScanInChoiceActivity.this, "Failed to check out all guests", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            // Handle the error
                        }
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