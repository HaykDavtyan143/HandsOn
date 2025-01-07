package com.example.handson1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivityPerson extends AppCompatActivity
{
    private EditText etEmail;
    private EditText etPassword;
    private Button btnSignUp, btnBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_person);

        // Catch unexpected exceptions
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e("AppCrash", "Unhandled exception: ", throwable);
            runOnUiThread(() ->
                    Toast.makeText(SignupActivityPerson.this, "Unexpected error occurred!", Toast.LENGTH_LONG).show()
            );
        });

        mAuth = FirebaseAuth.getInstance();


        etEmail = findViewById(R.id.edittextEmail);
        etPassword = findViewById(R.id.edittextPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnBack = findViewById(R.id.buttonBack);

        btnSignUp.setOnClickListener(v -> signUpUser());
        btnBack.setOnClickListener(v -> finish());
    }

    private void signUpUser()
    {
        if (etEmail == null || etPassword == null)
        {
            Toast.makeText(this, "UI elements not initialized properly!", Toast.LENGTH_SHORT).show();
            Log.e("Signup", "UI elements not initialized.");
            return;
        }

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation checks
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please enter email and password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(this, "Invalid email format!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6)
        {
            Toast.makeText(this, "Password must be at least 6 characters long!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the email already exists
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        boolean userExists = task.getResult() != null && !task.getResult().getSignInMethods().isEmpty();

                        if (userExists)
                        {
                            Toast.makeText(this, "User already exists with this email!", Toast.LENGTH_SHORT).show();
                            Log.d("Signup", "User already exists: " + email);
                        }

                        else
                        {
                            // Create a new user
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(signupTask -> {
                                        if (signupTask.isSuccessful())
                                        {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                            Log.d("Signup", "User signed up: " + (user != null ? user.getEmail() : "null"));

                                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("UserType", "Person");

                                            db.collection("users").document(userId)
                                                    .set(userData) // Overwrites the document if it exists
                                                    .addOnSuccessListener(aVoid -> {
                                                        // Successfully written to Firestore
                                                        Log.d("Firestore", "User details saved successfully.");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        // Error writing to Firestore
                                                        Log.e("Firestore", "Error saving user details: ", e);
                                                    });

                                            Intent intent = new Intent(SignupActivityPerson.this, LoginActivityPerson.class);
                                            startActivity(intent);
                                            finish();
                                        }

                                        else
                                        {
                                            Log.e("Signup", "Failed to sign up", signupTask.getException());
                                            Toast.makeText(this, "Signup failed: " + signupTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    else
                    {
                        Log.e("Signup", "Error checking user existence", task.getException());
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Signup", "Firebase operation failed", e);
                    Toast.makeText(this, "Firebase operation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
