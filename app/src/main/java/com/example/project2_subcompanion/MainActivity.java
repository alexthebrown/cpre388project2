
package com.example.project2_subcompanion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @author Alex Brown
 * Main landing page for all users. Only shows buttons to pages that user has the ability to visit.
 */
public class MainActivity extends AppCompatActivity {

    TextView greeting, volunteerPoints;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    Button btn_logout, btn_calendar, btn_addEvent, btn_checkIn, btn_userList, btn_execCheckIn, btn_readNFC;
    String name, email, userLevel;
    LinearLayout execButtons;
    Long points;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        btn_checkIn = findViewById(R.id.btn_checkIn);
        btn_userList = findViewById(R.id.btn_userList);
        btn_execCheckIn = findViewById(R.id.btn_execCheckIn);
        execButtons = findViewById(R.id.execButtons);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        volunteerPoints = findViewById(R.id.volunteerPointsTextView);
        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get the first document from the query results
                            name = document.getString("name");
                            email = document.getString("email");
                            userLevel = document.getString("userClass");
                            points = document.getLong("volunteerPoints");
                            assert userLevel != null;
                            if (userLevel.equals("exec")){
                                execButtons.setVisibility(View.VISIBLE);
                                btn_addEvent.setVisibility(View.VISIBLE);
                            }
                            if (document.getBoolean("checkedInAD")){
                                btn_checkIn.setVisibility(View.VISIBLE);
                            }
                            Log.d("Firestore Output", "Name: " + name + ", Email: " + email);
                            greeting.setText("Hello " + name + "!");
                            volunteerPoints.setText("Volunteer Points: " + points);
                        } else {
                            // No document found with the matching ID
                            name = currentUser.getEmail();
                            greeting.setText("Hello " + name + "!");
                            volunteerPoints.setVisibility(View.GONE);
                        }
                    } else {
                        // Handle errors while fetching the data
                        Log.e("FirestoreError", "Error fetching user data", task.getException());
                        name = currentUser.getEmail();

                        greeting.setText("Hello " + name + "!");
                        volunteerPoints.setVisibility(View.GONE);
                    }
                });



        btn_calendar = findViewById(R.id.btn_calendar);
        btn_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScheduleList.class);
                startActivity(intent);
            }
        });

        btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
        greeting = findViewById(R.id.greetingText);

        String url = "https://sub.iastate.edu/calendar";

        btn_addEvent = findViewById(R.id.btn_addEvent);
        btn_addEvent.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ExecCreateEvent.class);
                startActivity(intent);
            }
        });

        btn_userList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ListUsersActivity.class);
                startActivity(intent);
            }
        });

        btn_execCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScanInChoiceActivity.class);
                startActivity(intent);
            }
        });

        btn_checkIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create and show the alert dialog
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Confirm Check Out")
                        .setMessage("Are you sure youwant to check out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // User confirmed, proceed with checkUserOut()
                                checkUserOut();
                                btn_checkIn.setVisibility(View.GONE);
                            }
                        })
                        .setNegativeButton("No", null) // Do nothing if user cancels
                        .show();
            }
        });


//        btn_readNFC = findViewById(R.id.btn_readNFC);
//        btn_readNFC.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, TagReadActivity.class);
//                startActivity(intent);
//            }
//        });

    }

    private void checkUserOut() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();if (currentUser != null) {
            DocumentReference userRef = db.collection("users").document(currentUser.getUid());
            DocumentReference attendanceRef = db.collection("eventAttendance").document("attendance");

            // Update user's check-in status to false and then decrement "filled"
            userRef.update("checkedInAD", false)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // User checked out successfully, now decrement "filled"
                            attendanceRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        int filled = documentSnapshot.getLong("filled").intValue();

                                        if (filled > 0) {
                                            attendanceRef.update("filled", filled - 1)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            // Attendance updated successfully
                                                            Toast.makeText(MainActivity.this, "User checked out and attendance updated", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(MainActivity.this, "Failed to update attendance", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            // "filled" is already 0, no need to decrement
                                            Toast.makeText(MainActivity.this, "User checked out (attendancealready 0)", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // Attendance document not found
                                        Toast.makeText(MainActivity.this, "Attendance document not found", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Failed to fetch attendance document", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Failed to check out user", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Handle case where current user is null (not logged in)
            Toast.makeText(MainActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}