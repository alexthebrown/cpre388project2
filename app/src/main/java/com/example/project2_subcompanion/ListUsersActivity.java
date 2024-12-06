package com.example.project2_subcompanion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

public class ListUsersActivity extends AppCompatActivity implements RecyclerViewInterface {

    FirebaseFirestore firestore;
    RecyclerView recyclerView;
    UserAdapter adapter;
    ArrayList<UserModel> userList, filtered;
    TextView searchbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_users);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = findViewById(R.id.recyclerView);
        userList = new ArrayList<>();

        searchbar = findViewById(R.id.search_bar);
        searchbar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String input = charSequence.toString();
                if(input.isEmpty()){
                    filtered = userList;
                }
                else{
                    filtered = new ArrayList<>();
                    for (UserModel user : userList){
                        if(user.getName().toLowerCase().contains(input) || user.getEmail().toLowerCase().contains(input)){
                            filtered.add(user);
                        }
                    }
                }
                adapter = new UserAdapter(filtered, ListUsersActivity.this, ListUsersActivity.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firestore = FirebaseFirestore.getInstance();

        firestore.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                String id = document.getId();
                                String email = document.getString("email");
                                String name = document.getString("name");
                                String userClass = document.getString("userClass");

                                UserModel user = new UserModel(id, name, email, userClass);
                                userList.add(user);
                            }
                            filtered = userList;


                            adapter = new UserAdapter(filtered, ListUsersActivity.this, ListUsersActivity.this);
                            recyclerView.setAdapter(adapter);
                        } else {
                            Log.e("Firestore", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onItemClick(int position){
        String opposite;
        if(filtered.get(position).getUserClass().equals("exec")){
            opposite = "public";
        }
        else{
            opposite = "exec";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alter User?")
                .setMessage("Are you sure you want make " + filtered.get(position).getName() + " a " + opposite + " class user?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firestore.collection("users").document(filtered.get(position).getId())
                                        .update("userClass", opposite)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(ListUsersActivity.this, "Successfully Changed User Class", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(ListUsersActivity.this, "Failed to change user class", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                        dialog.dismiss(); // Close the dialog
                        // Add your "Yes" action here
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the No button click
                        dialog.dismiss(); // Close the dialog
                        // Add your "No" action here
                    }
                });

// Display the dialog
        AlertDialog alert = builder.create();
        alert.show();
    }
}