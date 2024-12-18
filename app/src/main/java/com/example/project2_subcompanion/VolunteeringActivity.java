package com.example.project2_subcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolunteeringActivity extends AppCompatActivity implements RecyclerViewInterface {

    private static final String TAG = "VolunteeringActivity";

    private TextView textViewExecNum, textViewVolunteerNum, textViewVolunteerTime;
    private RecyclerView recyclerViewExecs, recyclerViewGeneral;
    private Button volunteerButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    int execNeeded, generalNeeded;

    VolunteersAdapter execAdapter, generalAdapter;

    private String eventId; // Assuming you're passing the event ID to this activity
    private String userClass; // Assuming you have a way to determine the user's class (exec or general)

    ArrayList<UserModel> execs, general;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteering);

        // Initialize views
        textViewExecNum = findViewById(R.id.textViewExecNum);
        textViewVolunteerNum = findViewById(R.id.textViewVolunteerNum);
        textViewVolunteerTime = findViewById(R.id.textViewVolunteerTime);
        recyclerViewExecs = findViewById(R.id.recyclerViewExecs);
        recyclerViewGeneral = findViewById(R.id.recyclerViewGeneral);
        volunteerButton = findViewById(R.id.volunteer);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Get event ID and user class (replace with your logic)
        eventId = getIntent().getStringExtra("eventId");
        userClass = getUserClass(); // Replace with your logic to get user class

        // Set up RecyclerViews
        recyclerViewExecs.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGeneral.setLayoutManager(new LinearLayoutManager(this));

        // Fetch event data from Firestore
        fetchEventData();

        execs = new ArrayList<>();
        general = new ArrayList<>();


        // Volunteer button click listener
        volunteerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleVolunteerButtonClick();
            }
        });
    }

    private void fetchEventData() {
        DocumentReference eventRef = db.collection("signups").document(eventId);
        eventRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Update views with event data
                        execNeeded = document.getLong("execNeeded").intValue();
                        generalNeeded = document.getLong("generalNeeded").intValue();
                        textViewExecNum.setText(String.valueOf(document.getLong("execNum").intValue()));
                        textViewVolunteerNum.setText(String.valueOf(document.getLong("volNum").intValue()));
                        textViewVolunteerTime.setText(document.getString("volunteerTime"));

                        // Set up RecyclerView adapters (replace with your adapter logic)
                        List<DocumentReference> execsList = (List<DocumentReference>) document.get("execs");
                        for (DocumentReference userRef : execsList) {
                            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult(); // Get the single DocumentSnapshot
                                        if (document.exists()) {
                                            String id = document.getId();
                                            String email = document.getString("email");
                                            String name = document.getString("name");
                                            String userClass = document.getString("userClass");

                                            UserModel user = new UserModel(id, name, email, userClass);
                                            execs.add(user); // Add the user to the list
                                        } else {
                                            // User document not found
                                        }
                                        // Update the adapter after fetching each user
                                        execAdapter = new VolunteersAdapter(execs, VolunteeringActivity.this, VolunteeringActivity.this, currentUser.getUid());
                                        recyclerViewExecs.setAdapter(execAdapter);
                                    } else {
                                        // Handle the error
                                    }
                                }
                            });
                        }
                        List<DocumentReference> generalList = (List<DocumentReference>) document.get("general");
                        for (DocumentReference userRef : generalList) {
                            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult(); // Get the single DocumentSnapshot
                                        if (document.exists()) {
                                            String id = document.getId();
                                            String email = document.getString("email");
                                            String name = document.getString("name");
                                            String userClass = document.getString("userClass");

                                            UserModel user = new UserModel(id, name, email, userClass);
                                            execs.add(user); // Add the user to the list
                                        } else {
                                            // User document not found
                                        }
                                        // Update the adapter after fetching each user
                                        generalAdapter = new VolunteersAdapter(general, VolunteeringActivity.this, VolunteeringActivity.this, currentUser.getUid());
                                        recyclerViewGeneral.setAdapter(generalAdapter);
                                    } else {
                                        // Handle the error
                                    }
                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "No such document");}
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void handleVolunteerButtonClick() {
        if (currentUser == null) {
            // User not signed in, handle accordingly (e.g., show sign-in prompt)
            Toast.makeText(this, "Please sign in to volunteer.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference eventRef = db.collection("signups").document(eventId);

        if (getUserClass().equals("exec") && execs.size() < execNeeded) {
            eventRef.update("execs", FieldValue.arrayUnion(currentUser.getUid()))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(VolunteeringActivity.this, "Volunteered as exec!", Toast.LENGTH_SHORT).show();
                            // Update RecyclerView adapter or refresh data
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(VolunteeringActivity.this, "Failed to volunteer.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Error updating document", e);
                        }
                    });
        } else {
            // Similar logic for general volunteers
            if(general.size() < generalNeeded){
                eventRef.update("general", FieldValue.arrayUnion(currentUser.getUid()))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(VolunteeringActivity.this, "Volunteered as general!", Toast.LENGTH_SHORT).show();
                                // Update RecyclerView adapter or refresh data
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(VolunteeringActivity.this, "Failed to volunteer.", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Error updating document", e);
                            }
                        });
            }
        }
    }

    // Replace with your logic to get the user's class (exec or general)
    private String getUserClass() {
        final String[] userClass = new String[1]; // Default to exec
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                         userClass[0] = documentSnapshot.getString("userClass");
                    }
                });
        return userClass[0];
    }


    @Override
    public void onItemClick(int position) {
        if(general.get(position).getUserClass().equals(getUserClass())){
            general.remove(position);
        }
        else{
            execs.remove(position);

        };
    }
}