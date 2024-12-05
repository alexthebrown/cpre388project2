package com.example.project2_subcompanion;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExecCreateSignup extends AppCompatActivity {

    private TimePickerDialog timePickerDialog;
    private Button timeButton, submitButton;
    private TextView execNumTV, volNumTV;
    FirebaseFirestore firestore;
    Timestamp timestamp;
    int year, month, day;
    Calendar cal;
    Date date;

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
        cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        firestore = FirebaseFirestore.getInstance();
        initTimePicker();
        firestore = FirebaseFirestore.getInstance();
        timeButton = findViewById(R.id.timePickerButton);
        timeButton.setText("7:00");
        execNumTV = findViewById(R.id.execNum);
        volNumTV = findViewById(R.id.volNum);
        DocumentReference eventRef = firestore.collection("event").document(i.getStringExtra("eventID"));
        submitButton = findViewById(R.id.btn_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> signup = new HashMap<>();
                signup.put("execNum", String.valueOf(execNumTV.getText()));
                signup.put("volNum", String.valueOf(volNumTV.getText()));
                date = new Date(cal.getTimeInMillis());
                timestamp = new Timestamp(date);
                Log.e("TimeStamp Thing", timestamp.toString());
                signup.put("volunteerTime", timestamp);
                signup.put("eventID",eventRef);

                firestore.collection("signups")
                        .add(signup)
                        .addOnSuccessListener(documentReference -> {
                            // Successfully written
                            String signupId = documentReference.getId();

                            // Update the related event with the signup reference
                            Map<String, Object> eventUpdate = new HashMap<>();
                            eventUpdate.put("signup", firestore.collection("signups").document(signupId));
                            eventRef.update(eventUpdate)
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully updated
                                         Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                         startActivity(intent);
                                        Log.d("Firestore", "Event updated with signup reference");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Firestore", "Error updating event", e);
                                    });

                            Log.d("TAG", "DocumentSnapshot added!");
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

}