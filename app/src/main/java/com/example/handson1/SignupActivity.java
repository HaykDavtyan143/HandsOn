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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity
{
    private int type;
    private EditText etEmail;
    private EditText etPassword, etConfirmPassword, etUsername;
    private Button btnSignUp, btnBack;
    private RadioGroup check;
    private ImageButton passwordToggle1, passwordToggle2;
    private FirebaseAuth mAuth;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Catch unexpected exceptions
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e("AppCrash", "Unhandled exception: ", throwable);
            runOnUiThread(() ->
                    Toast.makeText(SignupActivity.this, "Unexpected error occurred!", Toast.LENGTH_LONG).show()
            );
        });

        mAuth = FirebaseAuth.getInstance();

        type = 0;

        etEmail = findViewById(R.id.edittextEmail);
        etPassword = findViewById(R.id.edittextPassword);
        etConfirmPassword = findViewById(R.id.edittextConfirmPassword);
        etUsername = findViewById(R.id.edittextUsername);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnBack = findViewById(R.id.buttonBack);
        check = findViewById(R.id.check);
        passwordToggle1 = findViewById(R.id.passwordToggle1);
        passwordToggle2=findViewById(R.id.passwordToggle2);
        btnSignUp.setOnClickListener(v -> signUpUser());



        passwordToggle1.setOnClickListener(v -> {
            if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod)
            {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle1.setImageResource(R.drawable.ic_eye_open);
            }
            else
            {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle1.setImageResource(R.drawable.ic_eye_closed);
            }

            etPassword.setSelection(etPassword.getText().length());
        });

        check.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.person)
            {
                type = 1;
            }

            else if (checkedId == R.id.organization)
            {
                type = 2;
            }
        });

        passwordToggle2.setOnClickListener(v -> {
            if (etConfirmPassword.getTransformationMethod() instanceof PasswordTransformationMethod)
            {
                etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle2.setImageResource(R.drawable.ic_eye_open);
            }

            else
            {
                etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle2.setImageResource(R.drawable.ic_eye_closed);
            }

            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });

        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void signUpUser()
    {
        if (etEmail == null || etPassword == null)
        {
            Toast.makeText(this, "UI elements not initialized properly!", Toast.LENGTH_SHORT).show();
            Log.e("Signup", "UI elements not initialized.");
            return;
        }

        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (type == 0)
        {
            Toast.makeText(this, "Please select account type!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please enter email, username and password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && TextUtils.isEmpty(confirmPassword))
        {
            Toast.makeText(this, "Please confirm the password", Toast.LENGTH_SHORT).show();
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
                            if (confirmPassword.equals(password))
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

                                                Map<String, Object> User = new HashMap<>();
                                                User.put("Email", email);
                                                User.put("Password", password);
                                                if (type == 1)
                                                {
                                                    User.put("Type", "Person");
                                                }

                                                else if (type == 2)
                                                {
                                                    User.put("Type", "Organization");
                                                }
                                                User.put("Username", username);


                                                db.collection("users").add(User) // Overwrites the document if it exists
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("Firestore", "User saved successfully.");
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e("Firestore", "Error saving user details: ", e);
                                                        });

                                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
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
                            else
                            {
                                Toast.makeText(this, "The passwords you entered do not match.", Toast.LENGTH_SHORT).show();
                            }
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
