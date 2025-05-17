package com.haykdavtyan.handson;

import android.os.Bundle;
import android.widget.NumberPicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class AccountCreateActivityPerson extends AppCompatActivity
{

    @Override

    protected void onCreate(Bundle savedInstanceState)
    {
        NumberPicker agePicker;

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_create_person);


        agePicker = findViewById(R.id.npAge);
        agePicker.setMinValue(1);
        agePicker.setMaxValue(120);
        agePicker.setValue(15);

    }
}