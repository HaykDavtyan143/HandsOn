package com.example.handson1;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity
{
    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignUp, btnTestUser;
    private ImageButton passwordToggle;
    private FirebaseAuth mAuth;

    @Override
    public void onStart()
    {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean fromSignup = getIntent().getBooleanExtra("fromSignup", false);

        if (currentUser != null)
        {
            Log.d("LoginActivity", "User is logged in: " + currentUser.getEmail());

            currentUser.reload().addOnCompleteListener(task -> {
                FirebaseUser updatedUser = mAuth.getCurrentUser();

                if (updatedUser == null)
                {
                    Log.d("LoginActivity", "User no longer exists in Firebase. Signing out...");
                    mAuth.signOut();
                    return;
                }

                Log.d("LoginActivity", "Email Verified: " + updatedUser.isEmailVerified());

                if (updatedUser.isEmailVerified() && !fromSignup)
                {
                    Log.d("LoginActivity", "Redirecting to FeedActivity...");
                    Intent intent = new Intent(LoginActivity.this, FeedActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Log.d("LoginActivity", "User is logged in but email is NOT verified or came from Signup.");
                }
            });

        }
        else
        {
            Log.d("LoginActivity", "User is NOT logged in. Staying on LoginActivity.");
        }
    }







    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnTestUser = findViewById(R.id.btntestUser);
        passwordToggle = findViewById(R.id.password_Toggle);


        btnLogin.setOnClickListener(v -> loginUser());

        btnTestUser.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                logInTestUser();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        passwordToggle.setOnClickListener(v -> {
            if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod)
            {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.ic_eye_open);
            }
            else
            {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.ic_eye_closed);
            }

            etPassword.setSelection(etPassword.getText().length());
        });
    }

    private void logInTestUser ()
    {
        String testEmail = "testuser7887handson143@gmail.com";
        String testPassword = "forhandsOn1";

        mAuth.signInWithEmailAndPassword(testEmail, testPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful())
                    {
                        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                        startActivity(intent);
                        finish();

                        Toast.makeText(LoginActivity.this, "Logged in as Test User", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "Test login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loginUser()
    {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
        {
            Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful())
                    {
                        if (mAuth.getCurrentUser().isEmailVerified()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Log.d("Login", "User logged in: " + user.getEmail());

                            Intent intent = new Intent(LoginActivity.this, FeedActivity.class);
                            startActivity(intent);
                        }

                        else
                        {
                            Toast.makeText(LoginActivity.this, "Please verify your email address first", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                        }
                    }

                    else
                    {
                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Login", "Failed to log in", task.getException());
                    }
                });
    }
}
