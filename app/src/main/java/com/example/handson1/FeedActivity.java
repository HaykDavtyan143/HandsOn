package com.example.handson1;

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

public class FeedActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FeedAdapter feedAdapter;
    protected List<Post> posts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(this, posts);
        recyclerView.setAdapter(feedAdapter);

        fetchPostsFromFirestore();

        if (savedInstanceState == null)
        {
           getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NavigationBarFragment())
                    .commit();
        }
    }

    public void fetchPostsFromFirestore()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    posts.clear();

                    for (QueryDocumentSnapshot document : querySnapshot)
                    {
                        try
                        {
                            Post post = document.toObject(Post.class);

                            if (document.getId() != null)
                            {
                                post.setId(document.getId());
                            }
                            else
                            {
                                Log.e("FeedActivity", "Post document ID is null: " + document.getData());
                                continue;
                            }

                            Object commentsObject = document.get("comments");

                            if (commentsObject instanceof Map)
                            {
                                HashMap<String, Object> fixedComments = new HashMap<>((Map<String, Object>) commentsObject);
                                post.setComments(fixedComments);
                            }
                            else
                            {
                                post.setComments(new HashMap<>());
                                Log.e("FeedActivity", "Unexpected comments format: " + commentsObject);
                            }

                            Log.d("FeedActivity", "Fetched post ID: " + post.getId() + ", Comments: " + post.getComments());

                            posts.add(post);

                        }
                        catch (Exception e)
                        {
                            Log.e("FeedActivity", "Error processing post: " + document.getId(), e);
                        }
                    }

                    runOnUiThread(() -> feedAdapter.notifyDataSetChanged());
                })
                .addOnFailureListener(e -> Log.e("FeedActivity", "Error fetching posts", e));
    }
}
