package com.example.project2_subcompanion;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExecCreateSignup extends AppCompatActivity {

    private TimePickerDialog timePickerDialog;
    private Button timeButton, submitButton;
    private TextView execNumTV, volNumTV, heading;
    FirebaseFirestore firestore;
    Timestamp timestamp;
    int year, month, day;
    Calendar cal;
    Date date;
    String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exec_create_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent i = getIntent();
        eventId = i.getStringExtra("eventID");
        cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        firestore = FirebaseFirestore.getInstance();
        initTimePicker();
        firestore = FirebaseFirestore.getInstance();
        timeButton = findViewById(R.id.timePickerButton);
        int i0 = cal.get(Calendar.HOUR_OF_DAY);
        int i1 = cal.get(Calendar.MINUTE);
        timeButton.setText(makeTimeString(i0, i1));
        execNumTV = findViewById(R.id.execNum);
        volNumTV = findViewById(R.id.volNum);
        heading = findViewById(R.id.heading);
        getExisting();
        DocumentReference eventRef = firestore.collection("event").document(eventId);
        submitButton = findViewById(R.id.btn_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> signup = new HashMap<>();
                signup.put("execNum", Integer.valueOf(String.valueOf(execNumTV.getText())));
                signup.put("volNum", Integer.valueOf(String.valueOf(volNumTV.getText())));
                date = new Date(cal.getTimeInMillis());
                timestamp = new Timestamp(date);
                Log.e("TimeStamp Thing", timestamp.toString());
                signup.put("volunteerTime", timestamp);
                signup.put("eventID",eventRef);

                firestore.collection("signups").document(eventRef.getId())
                        .set(signup)
                        .addOnSuccessListener(aVoid -> {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            // Handle the error
                            Log.e("Firestore", "Error writing document", e);
                        });
            }

        });
    }

    private void initTimePicker() {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    cal.set(Calendar.HOUR_OF_DAY, i);
                    cal.set(Calendar.MINUTE, i1);
                    timeButton.setText(makeTimeString(i, i1));

                }
        };

        timePickerDialog = new TimePickerDialog(this, timeSetListener, 6, 45, false);
    }

    private String makeTimeString(int hour, int minute){
        return "" + hour + ":" + minute;
    }

    public void openTimePicker(View view){
        timePickerDialog.show();
    }

    private void getExisting(){
        firestore.collection("signups").document(eventId)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> incoming = document.getData();
                            heading.setText("Edit Signup");
                            execNumTV.setText(String.valueOf(incoming.get("execNum")));
                            volNumTV.setText(String.valueOf(incoming.get("volNum")));
                            Timestamp incomingTimestamp = (Timestamp) incoming.get("volunteerTime");
                            Date incomingDate = incomingTimestamp.toDate();
                            cal.setTime(incomingDate);
                            int i = cal.get(Calendar.HOUR_OF_DAY);
                            int i1 = cal.get(Calendar.MINUTE);
                            timeButton.setText(makeTimeString(i, i1));
                            Log.d("Old Signup data", "DocumentSnapshot data: " + incoming);
                        }
                        else{
                            Log.d("Old Signup data", "No such document");
                        }

                    } else {
                        Log.e("Old Signup data", "Error getting documents: ", task.getException());
                    }
                });
    }

}