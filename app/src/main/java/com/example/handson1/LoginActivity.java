package com.example.handson1;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity
{
    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignUp, btnTestUser;
    private ImageButton passwordToggle;
    private FirebaseAuth mAuth;

    private User currUser;

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
                    fetchCurrentUsername(username -> {
                        fetchCurrentAccType(accType -> {
                            currUser = new User(updatedUser.getUid(), username, accType);
                        });
                    });
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


    public User getCurrUser ()
    {
        return currUser;
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

                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Log.d("Login", "Guest logged in");
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "Guest login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                        if (mAuth.getCurrentUser().isEmailVerified())
                        {
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

    private void fetchCurrentUsername(AccTypeCallback callback)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(uid);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists())
                {
                    DocumentSnapshot document = task.getResult();
                    String username = document.getString("Username");
                    callback.onAccTypeRetrieved(username);
                }
                else
                {
                    callback.onAccTypeRetrieved(null);
                }
            });
        }
        else
        {
            callback.onAccTypeRetrieved(null);
        }
    }

    private void fetchCurrentAccType(UsernameCallback callback)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(uid);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists())
                {
                    DocumentSnapshot document = task.getResult();
                    String username = document.getString("Type");
                    callback.onUsernameRetrieved(username);
                }
                else
                {
                    callback.onUsernameRetrieved(null);
                }
            });
        }
        else
        {
            callback.onUsernameRetrieved(null);
        }
    }
}
interface AccTypeCallback
{
    void onAccTypeRetrieved(String username);
}
interface UsernameCallback
{
    void onUsernameRetrieved(String username);
}


