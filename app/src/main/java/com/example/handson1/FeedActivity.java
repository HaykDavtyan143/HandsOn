package com.example.handson1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private FeedAdapter feedAdapter;
    private List<Post> posts = new ArrayList<>();

    private ImageButton btnHome, btnSearch, btnAddPost, btnMessages, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(this, posts);
        recyclerView.setAdapter(feedAdapter);
        btnHome = findViewById(R.id.ib_home);
        btnSearch = findViewById(R.id.ib_search);
        btnAddPost = findViewById(R.id.ib_add_post);
        btnMessages = findViewById(R.id.ib_messages);
        btnProfile = findViewById(R.id.ib_profile);

        fetchPostsFromFirestore();

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, FeedActivity.class);
            startActivity(intent);
            finish();
        });

        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, SearchActivity.class);
            startActivity(intent);
            finish();
        });

        btnAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, AddPostActivity.class);
            startActivity(intent);
        });

        btnMessages.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, MessagesActivity.class);
            startActivity(intent);
            finish();
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchPostsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    posts.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        try {
                            // Convert the document to a Post object
                            Post post = document.toObject(Post.class);

                            // Ensure the post object has a valid Firestore document ID
                            if (document.getId() != null) {
                                post.setId(document.getId());
                            } else {
                                Log.e("FeedActivity", "Post document ID is null: " + document.getData());
                                continue; // Skip this post if ID is null
                            }

                            // Handle the comments field if it's present
                            if (document.contains("comments"))
                            {
                                Object commentsObject = document.get("comments");
                                if (commentsObject instanceof List)
                                {
                                    post.setComments((List<Map<String, Object>>) commentsObject);
                                }
                                else
                                {
                                    post.setComments(new ArrayList<>());
                                    Log.e("FeedActivity", "Unexpected comments format: " + commentsObject);
                                }
                            }

                            posts.add(post);
                        } catch (Exception e)
                        {
                            Log.e("FeedActivity", "Error processing post: " + document.getId(), e);
                        }
                    }

                    // Notify the adapter that the data has changed
                    feedAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("FeedActivity", "Error fetching posts", e));
    }

    private Map<String, Object> createPostMap(String title, String description)
    {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("title", title);
        postMap.put("description", description);
        postMap.put("comments", new ArrayList<String>());
        postMap.put("likes", 0);
        return postMap;
    }
}
