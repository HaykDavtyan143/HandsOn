package com.haykdavtyan.handson;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null)
        {
            Log.d("MainActivity", "User is logged in: " + currentUser.getEmail());

            currentUser.reload().addOnCompleteListener(task -> {
                FirebaseUser updatedUser = mAuth.getCurrentUser();
                if (updatedUser != null && updatedUser.isEmailVerified())
                {
                    Log.d("MainActivity", "Email verified. Redirecting to FeedActivity...");
                    Intent intent = new Intent(com.haykdavtyan.handson.MainActivity.this, FeedActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Log.d("MainActivity", "Email not verified or user session expired. Redirecting to LoginActivity...");
                    Intent intent = new Intent(com.haykdavtyan.handson.MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

                finish();
            });

        }
        else
        {
            Log.d("MainActivity", "User is NOT logged in. Redirecting to LoginActivity");
            Intent intent = new Intent(com.haykdavtyan.handson.MainActivity.this, LoginActivity.class);
            startActivity(intent);

            finish();
        }
    }
}
