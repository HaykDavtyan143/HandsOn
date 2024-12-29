package com.example.handson1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccountCreateActivityOrganization extends AppCompatActivity
{

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        Spinner sType, sIS;

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_create_organization);

        sType = findViewById(R.id.sFOA);
        List<String> typesList = Arrays.asList(
                "Select type", "",
                "Company Limited by Shares", "Company Limited by Guarantee",
                "Unlimited Company", "One Person Company",
                "Private Company", "Public Company",
                "Holding and Subsidiary Company", "Associate Company",
                "Company in terms of Access to Capital", "Government Company",
                "Foreign Company", "Charitable Company",
                "Dormant Company", "Nidhi Companies",
                "Public Financial Institutions");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sType.setAdapter(adapter);

        sIS = findViewById(R.id.sFOA);
        List<String> ActFIeList = Arrays.asList();
    }
}