package com.example.project2_subcompanion;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class VolunteeringActivity extends AppCompatActivity implements RecyclerViewInterface {

    private static final String TAG = "VolunteeringActivity";

    private TextView textViewExecNum, textViewVolunteerNum, textViewVolunteerTime;
    private RecyclerView recyclerViewExecs, recyclerViewGeneral;
    private Button volunteerButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private String eventId;
    private String userClass;
    int execNeeded, generalNeeded;


    private VolunteersAdapter execAdapter, generalAdapter;
    private ArrayList<UserModel> execs = new ArrayList<>();
    private ArrayList<UserModel> general = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteering);

        // Initialize views
        textViewExecNum =findViewById(R.id.textViewExecNum);
        textViewVolunteerNum = findViewById(R.id.textViewVolunteerNum);
        textViewVolunteerTime = findViewById(R.id.textViewVolunteerTime);
        recyclerViewExecs = findViewById(R.id.recyclerViewExecs);
        recyclerViewGeneral = findViewById(R.id.recyclerViewGeneral);
        volunteerButton = findViewById(R.id.volunteer);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Get event ID
        eventId = getIntent().getStringExtra("eventId");

        // Set up RecyclerViews with LayoutManagers
        recyclerViewExecs.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGeneral.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapters
        execAdapter = new VolunteersAdapter(execs, this, this, currentUser.getUid());
        generalAdapter = new VolunteersAdapter(general, this, this, currentUser.getUid());
        recyclerViewExecs.setAdapter(execAdapter);
        recyclerViewGeneral.setAdapter(generalAdapter);

        // Fetch data and update UI
        fetchDataAndUpdateUI();

        // Volunteer button click listener
        volunteerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleVolunteerButtonClick();
            }
        });
    }

    private void fetchDataAndUpdateUI() {
        // 1. Get user class
        getUserClass(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> userClassTask) {
                if (userClassTask.isSuccessful()) {
                    userClass = userClassTask.getResult();

                    // 2. Fetch event data
                    fetchEventData(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> eventTask) {
                            if (eventTask.isSuccessful()) {
                                DocumentSnapshot eventDoc = eventTask.getResult();
                                if (eventDoc.exists()) {
                                    // Update views with event data
                                    execNeeded = eventDoc.getLong("execNeeded").intValue();
                                    generalNeeded = eventDoc.getLong("generalNeeded").intValue();
                                    textViewExecNum.setText(String.valueOf(eventDoc.getLong("execNum").intValue()));
                                    textViewVolunteerNum.setText(String.valueOf(eventDoc.getLong("volNum").intValue()));
                                    textViewVolunteerTime.setText(eventDoc.getString("volunteerTime")); // Assuming "volunteerTime" is a string

                                    // 3. Fetch and populate volunteers
                                    fetchAndPopulateVolunteers(eventDoc, "execs", execs, execAdapter);
                                    fetchAndPopulateVolunteers(eventDoc, "general", general, generalAdapter);
                                } else {
                                    Log.d(TAG, "Event document not found");
                                }
                            } else {
                                Log.d(TAG, "Failed to fetch event data: ", eventTask.getException());
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "Failed to fetch user class: ", userClassTask.getException());
                }
            }
        });
    }

    private void getUserClass(OnCompleteListener<String> listener) {
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        listener.onComplete(Tasks.forResult(documentSnapshot.getString("userClass")));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onComplete(Tasks.forException(e));
                    }
                });
    }

    private void fetchEventData(OnCompleteListener<DocumentSnapshot> listener) {
        db.collection("signups").document(eventId).get()
                .addOnCompleteListener(listener);
    }

    private void fetchAndPopulateVolunteers(DocumentSnapshot eventDoc, String field, ArrayList<UserModel> list, VolunteersAdapter adapter) {
        List<DocumentReference> userRefs = (List<DocumentReference>) eventDoc.get(field);
        if (userRefs != null) {
            for (DocumentReference userRef : userRefs) {
                userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if (document.exists()) {
                            String id = document.getId();
                            String email = document.getString("email");
                            String name =document.getString("name");
                            String userClass = document.getString("userClass");

                            UserModel user = new UserModel(id, name, email, userClass);
                            list.add(user);
                        }
                        adapter.updateVolunteers(list);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to fetch user data: ", e);
                    }
                });
            }
        }
    }

    private void handleVolunteerButtonClick() {
        if (currentUser == null) {
            // User not signed in, handle accordingly (e.g., show sign-in prompt)
            Toast.makeText(this, "Please sign in to volunteer.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference eventRef = db.collection("signups").document(eventId);

        if (getUserClass().equals("exec") && execs.size() < execNeeded) {
            eventRef.update("execs", FieldValue.arrayUnion(db.collection("users").document(currentUser.getUid())))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(VolunteeringActivity.this, "Volunteered as exec!", Toast.LENGTH_SHORT).show();
                            // Update RecyclerView adapter or refresh data
                            fetchDataAndUpdateUI(); // Refresh data after volunteering
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(VolunteeringActivity.this, "Failed to volunteer.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Error updating document", e);
                        }
                    });
        } else if (getUserClass().equals("general") && general.size() < generalNeeded) {
            // Similar logic for general volunteers
            eventRef.update("general", FieldValue.arrayUnion(db.collection("users").document(currentUser.getUid())))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(VolunteeringActivity.this, "Volunteered as general!", Toast.LENGTH_SHORT).show();
                            // Update RecyclerView adapter or refresh data
                            fetchDataAndUpdateUI(); // Refresh data after volunteering
                        }

        })
                    .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(VolunteeringActivity.this, "Failed to volunteer.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error updating document", e);
            }
        });}
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
}@Override
public void onItemClick(int position) {
    // Handle item click (e.g., remove volunteer)
    // ...
}
}