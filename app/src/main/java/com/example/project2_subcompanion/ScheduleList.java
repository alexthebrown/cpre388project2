package com.example.project2_subcompanion;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ScheduleList extends AppCompatActivity implements RecyclerViewInterface {

    FirebaseFirestore firestore;
    RecyclerView recyclerView;
    CalendarAdapter adapter;
    ArrayList<CalendarModel> calendarItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        calendarItems = new ArrayList<>();

        // Set Layout Manager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        List<CalendarModel> calendarList = Arrays.asList(new CalendarModel("Event 1", "Date 1", "Location 1", "Price 1"),new CalendarModel("Event 2", "Date 2", "Location 2", "Price 2"), new CalendarModel("Event 3", "Date 3", "Location 3", "Price 3"));
//        CalendarAdapter adapter = new CalendarAdapter(calendarList);
//        recyclerView.setAdapter(adapter);
        firestore = FirebaseFirestore.getInstance();

        fetchData();

        firestore.collection("events").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(DocumentSnapshot document : task.getResult()) {
                        Log.d("Firestore Output", document.getString("name"));
                        String title = document.getString("name");
                        Timestamp date = document.getTimestamp("date");
                        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy HH:mm", Locale.getDefault());
                        String formattedTimestampString = dateFormat.format(date.toDate());
                        String location = document.getString("location");
                        String price = document.getString("price");

                        CalendarModel calendarItem = new CalendarModel(title, formattedTimestampString, location, price);
                        calendarItems.add(calendarItem);
                    }

                    // Set adapter after data is fetched
                    adapter = new CalendarAdapter(calendarItems);
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.e("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });
    }



    @Override
    public void onItemClick(int position) {
        // Handle item click here
        Log.d("RecyclerView", "Item clicked at position: " + position);

    }
}