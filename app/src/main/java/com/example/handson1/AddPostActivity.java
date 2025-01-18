package com.example.handson1;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextDescription;
    private Button buttonAddPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        buttonAddPost = findViewById(R.id.button_add_post);

        buttonAddPost.setOnClickListener(v -> addPostToFirestore());
    }

    private void addPostToFirestore() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("Title is required");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            editTextDescription.setError("Description is required");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new post object
        Map<String, Object> post = new HashMap<>();
        post.put("title", title);
        post.put("description", description);
        post.put("comments", new ArrayList<String>());

        // Add to Firestore
        db.collection("posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Post added successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity and return to the previous one
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add post: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
