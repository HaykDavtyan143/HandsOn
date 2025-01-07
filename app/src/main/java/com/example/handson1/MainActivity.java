package com.example.handson1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
{
    private Button btnPer;
    private Button btnOrg;
    private Button btnLog;
    private Button btnSign;
    private Button btnBack;
    private TextView tvLS;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPer = findViewById(R.id.ButtonPer);
        btnOrg = findViewById(R.id.ButtonOrg);
        btnLog = findViewById(R.id.buttonLogIn);
        btnSign = findViewById(R.id.buttonSignUp);
        btnBack = findViewById(R.id.buttonBack);
        tvLS = findViewById(R.id.tvls);

        btnPer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tvLS.setVisibility(View.VISIBLE);
                btnLog.setVisibility(View.VISIBLE);
                btnSign.setVisibility(View.VISIBLE);
                btnBack.setVisibility(View.VISIBLE);

                btnBack.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        tvLS.setVisibility(View.GONE);
                        btnLog.setVisibility(View.GONE);
                        btnSign.setVisibility(View.GONE);
                        btnBack.setVisibility(View.GONE);
                    }
                });

                btnLog.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent (MainActivity.this, LoginActivityPerson.class);
                        startActivity(intent);
                        finish();
                    }
                });

                btnSign.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent (MainActivity.this, SignupActivityPerson.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });

        btnOrg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tvLS.setVisibility(View.VISIBLE);
                btnLog.setVisibility(View.VISIBLE);
                btnSign.setVisibility(View.VISIBLE);
                btnBack.setVisibility(View.VISIBLE);

                btnBack.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        tvLS.setVisibility(View.GONE);
                        btnLog.setVisibility(View.GONE);
                        btnSign.setVisibility(View.GONE);
                        btnBack.setVisibility(View.GONE);
                    }
                });

                btnLog.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent (MainActivity.this, LoginActivityOrganization.class);
                        startActivity(intent);
                        finish();
                    }
                });

                btnSign.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(MainActivity.this, SignupActivityOrganization.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });


    }

}
