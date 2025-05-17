package com.haykdavtyan.handson;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MessagesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatFragment())
                    .commit();
        }
    }

}