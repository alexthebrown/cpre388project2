package com.example.project2_subcompanion;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Map;
/**
 * @author Alex Brown
 * Unused code but im scared to delete stuff
 */
public class FirebaseEventUploader {

    private FirebaseFirestore db;

    public FirebaseEventUploader() {
        db = FirebaseFirestore.getInstance();
    }

    public void uploadNewEvents(List<Map<String, String>> events) {
        for (Map<String, String> event : events) {
            String eventTitle = event.get("id");

            // Check if the event already exists
            db.collection("events")
                    .whereEqualTo("id", eventTitle)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        boolean exists = false;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            if (doc.exists()) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            // Add new event
                            db.collection("events")
                                    .document(eventTitle)
                                    .set(event)
                                    .addOnSuccessListener(documentReference ->
                                            System.out.println("Event added: " + eventTitle))
                                    .addOnFailureListener(e ->
                                            System.err.println("Error adding event: " + e.getMessage()));
                        }
                    })
                    .addOnFailureListener(e ->
                            System.err.println("Error checking event: " + e.getMessage()));
        }
    }
}

