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

public class LoginActivityPerson extends AppCompatActivity
{
    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignUp, btnBack;
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //stex intent himnakan page i vra
        }
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_person);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBack = findViewById(R.id.buttonBack);

        btnSignUp.setOnClickListener(v -> signUpUser());
        btnLogin.setOnClickListener(v -> loginUser());

        btnBack = findViewById(R.id.buttonBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(LoginActivityPerson.this, MainActivity.class);
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
            Toast.makeText(LoginActivityPerson.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful())
                    {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivityPerson.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                        Log.d("Signup", "User signed up: " + user.getEmail());
                    }

                    else
                    {
                        Toast.makeText(LoginActivityPerson.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Signup", "Failed to sign up", task.getException());
                    }
                });
    }

    private void loginUser()
    {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            Toast.makeText(LoginActivityPerson.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful())
                    {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivityPerson.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Log.d("Login", "User logged in: " + user.getEmail());

                        Intent intent = new Intent (LoginActivityPerson.this, AccountCreateActivityPerson.class);
                        startActivity(intent);
                    }

                    else
                    {
                        Toast.makeText(LoginActivityPerson.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Login", "Failed to log in", task.getException());
                    }
                });
    }
}
