package com.example.project2_subcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class CalendarEventFullDetailActivity extends AppCompatActivity {
    TextView title, date, location, price, description;
    String documentID;
    Button edit, signup, volunteer;
    FirebaseFirestore firestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar_event_full_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        title = findViewById(R.id.event_name);
        date = findViewById(R.id.date);
        location = findViewById(R.id.location);
        price = findViewById(R.id.price);
        description = findViewById(R.id.event_desc);
        edit = findViewById(R.id.btn_edit);
        volunteer = findViewById(R.id.btn_volunteer);
        Intent i = getIntent();
        documentID = i.getStringExtra("id");
        signup = findViewById(R.id.btn_signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ExecCreateSignup.class);
                intent.putExtra("eventID", documentID);
                startActivity(intent);
            }
        });
        firestore = FirebaseFirestore.getInstance();
        firestore.collection("events").document(documentID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                title.setText(task.getResult().getString("name"));
                Timestamp dateRec = task.getResult().getTimestamp("date");
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy hh:mm", Locale.getDefault());
                date.setText(dateFormat.format(dateRec.toDate()));
                location.setText(task.getResult().getString("location"));
                price.setText(task.getResult().getString("price"));
                description.setText(task.getResult().getString("description"));
            } else {
                // Handle the error
                // ...
            }
        });

            }

}