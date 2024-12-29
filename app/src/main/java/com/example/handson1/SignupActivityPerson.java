package com.example.handson1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivityPerson extends AppCompatActivity
{
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnSignUp, btnBack;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_person);

        mAuth = FirebaseAuth.getInstance();

        EditText etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnBack = findViewById(R.id.buttonBack);

        btnSignUp.setOnClickListener(v -> signUpUser());

        btnBack = findViewById(R.id.buttonBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SignupActivityPerson.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void signUpUser()
    {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            Toast.makeText(SignupActivityPerson.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        boolean userExists = task.getResult() != null && !task.getResult().getSignInMethods().isEmpty();

                        if (userExists)
                        {
                            // User already exists
                            Toast.makeText(SignupActivityPerson.this, "User already exists with this email!", Toast.LENGTH_SHORT).show();
                            Log.d("Signup", "User already exists: " + email);
                        }
                        else
                        {
                            // User does not exist, proceed with signup
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(SignupActivityPerson.this, signupTask -> {
                                        if (signupTask.isSuccessful())
                                        {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            Toast.makeText(SignupActivityPerson.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                            Log.d("Signup", "User signed up: " + user.getEmail());
                                        }
                                        else
                                        {
                                            Toast.makeText(SignupActivityPerson.this, "Signup failed: " + signupTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            Log.e("Signup", "Failed to sign up", signupTask.getException());
                                        }
                                    });
                        }
                    }
                    else
                    {
                        Toast.makeText(SignupActivityPerson.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Signup", "Error checking user existence", task.getException());
                    }
                });

    }
}
