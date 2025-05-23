package com.haykdavtyan.handson;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editUsername, editPassword;
    private RadioGroup radioGroup;
    private RadioButton volunteerRadio, organizationRadio;
    private Button saveButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        radioGroup = findViewById(R.id.check);
        volunteerRadio = findViewById(R.id.volunteer);
        organizationRadio = findViewById(R.id.organization);
        saveButton = findViewById(R.id.button_save);

        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                String newUsername = editUsername.getText().toString().trim();
                String newPassword = editPassword.getText().toString().trim();
                String newType = volunteerRadio.isChecked() ? "Volunteer" :
                        organizationRadio.isChecked() ? "Organization" : "";

                if (newUsername.isEmpty() || newPassword.isEmpty() || newType.isEmpty())
                {
                    Toast.makeText(EditProfileActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

                if (uid == null)
                {
                    Toast.makeText(EditProfileActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("Username", newUsername);
                updates.put("password", newPassword);
                updates.put("Type", newType);

                db.collection("users").document(uid)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(EditProfileActivity.this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}
