package com.example.project2_subcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    TextView emailBox, passwordBox, passwordRepeatBox, nameBox, clickHere, studentIdBox;
    Button btn_register;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        emailBox = findViewById(R.id.email);
        passwordBox = findViewById(R.id.password);
        passwordRepeatBox = findViewById(R.id.passwordRepeat);
        nameBox = findViewById(R.id.name);
        studentIdBox = findViewById(R.id.studentID);
        clickHere = findViewById(R.id.clickHere);
        clickHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Register.this, "I am clicked", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
            }
        });
        btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = String.valueOf(emailBox.getText());
                String password = String.valueOf(passwordBox.getText());
                String name = String.valueOf(nameBox.getText());
                Integer idNum = Integer.valueOf(String.valueOf(studentIdBox.getText()));

                if(TextUtils.isEmpty(email)){
                    emailBox.setError("Please enter email");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    passwordBox.setError("Please enter password");
                    return;
                }
                if(!password.equals(String.valueOf(passwordRepeatBox.getText()))){
                    passwordRepeatBox.setError("Passwords do not match");
                    return;
                }
                if(TextUtils.isEmpty(name)){
                    nameBox.setError("Please enter name");
                    return;
                }
                if(TextUtils.isEmpty(String.valueOf(idNum))){
                    studentIdBox.setError("Please enter Student ID Number");
                    return;
                }


                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("name", name);
                                    user.put("email", email);
                                    user.put("studentID", idNum);
                                    user.put("userClass", "public");
                                    user.put("volunteerPoints", 0);
                                    user.put("checkedInAD", false);

// Add a new document with a generated ID
                                    db.collection("users").document(currentUser.getUid())
                                            .set(user)
                                            .addOnSuccessListener(aVoid -> {
                                                // Successfully written
                                                Log.d("TAG", "DocumentSnapshot added!");
                                            })
                                            .addOnFailureListener(e -> {
                                                // Handle the error
                                                Log.e("Firestore", "Error writing document", e);
                                            });
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Register.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });
    }
}