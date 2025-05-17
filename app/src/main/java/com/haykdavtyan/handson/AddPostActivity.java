package com.haykdavtyan.handson;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity
{

    private EditText editTextTitle, editTextDescription;
    private Button buttonAddPost;

    private FeedActivity feedActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        buttonAddPost = findViewById(R.id.button_add_post);

        feedActivity = new FeedActivity();

        buttonAddPost.setOnClickListener(v -> addPostToFirestore());

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NavigationBarFragment())
                    .commit();
        }
    }

    private void addPostToFirestore()
    {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String creatorId;

        if (user != null)
        {
            creatorId = user.getUid();
        }
        else
        {
            creatorId = "unknown";
        }

        if (TextUtils.isEmpty(title))
        {
            editTextTitle.setError("Title is required");
            return;
        }

        if (TextUtils.isEmpty(description))
        {
            editTextDescription.setError("Description is required");
            return;
        }

        fetchCurrentUsername(username -> {
            if (username != null)
            {
                fetchCurrentAccType(type -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                Map<String, Object> likedBy = new HashMap<>();
                Map<String, Object> comments = new HashMap<>();
                Map<String, Object> post = new HashMap<>();

                post.put("creator", username);
                post.put("title", title);
                post.put("description", description);
                post.put("comments", comments);
                post.put("likes", 0);
                post.put("likedBy", likedBy);
                post.put("creatorID", creatorId);
                post.put("creatorType", type);

                db.collection("posts")
                        .add(post)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Post added successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to add post: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                });
            }
            else
            {
                Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
            }
        });

        feedActivity.fetchPostsFromFirestore();
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
