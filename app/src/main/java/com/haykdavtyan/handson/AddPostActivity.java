package com.haykdavtyan.handson;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import androidx.gridlayout.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity
{

    private EditText editTextTitle, editTextDescription;
    private Button buttonAddPost;

    private FeedActivity feedActivity;

    private Timestamp expirationTimestamp;

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

        GridLayout gridLayout = findViewById(R.id.select);
        int childCount = gridLayout.getChildCount();

        feedActivity = new FeedActivity();

        buttonAddPost.setOnClickListener(v -> addPostToFirestore());

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NavigationBarFragment())
                    .commit();
        }

        for (int i = 0; i < childCount; i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof RadioButton) {
                RadioButton rb = (RadioButton) child;
                rb.setOnClickListener(v -> {
                    // Uncheck all other RadioButtons
                    for (int j = 0; j < childCount; j++) {
                        View otherChild = gridLayout.getChildAt(j);
                        if (otherChild instanceof RadioButton && otherChild != v) {
                            ((RadioButton) otherChild).setChecked(false);
                        }
                    }
                    rb.setChecked(true);
                });
            }
        }


        TextView expirationDateView = findViewById(R.id.text_view_expiration_date);

        expirationDateView.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this, R.style.DatePickerDialogTheme,
                    (view, year1, month1, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year1);
                        calendar.set(Calendar.MONTH, month1);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        Calendar today = Calendar.getInstance();
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MILLISECOND, 0);

                        if (calendar.after(today)) {
                            expirationTimestamp = new Timestamp(calendar.getTime());

                            String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                    .format(calendar.getTime());
                            expirationDateView.setText(formattedDate);
                        } else {
                            Toast.makeText(this, "Expiration date must be in the future", Toast.LENGTH_SHORT).show();
                        }
                    },
                    year, month, day
            );


            datePickerDialog.show();
        });

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

        String category = getSelectedCategory();

        if (category == null) {
            Toast.makeText(this, "Please select a post category", Toast.LENGTH_SHORT).show();
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
                post.put("creationTime", Timestamp.now());
                post.put("expirationTime", expirationTimestamp);
                post.put("Category", category);
                post.put("approved", false);

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

    private String getSelectedCategory()
    {
        GridLayout gridLayout = findViewById(R.id.select);
        int childCount = gridLayout.getChildCount();

        for (int i = 0; i < childCount; i++)
        {
            View child = gridLayout.getChildAt(i);
            if (child instanceof RadioButton)
            {
                RadioButton rb = (RadioButton) child;
                if (rb.isChecked())
                {
                    return rb.getText().toString();
                }
            }
        }
        return null;
    }

}
