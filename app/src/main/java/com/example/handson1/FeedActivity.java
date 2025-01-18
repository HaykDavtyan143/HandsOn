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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(this, posts);
        recyclerView.setAdapter(feedAdapter);

        uploadPosts();
        fetchPostsFromFirestore();

        ImageButton fabAddPost = findViewById(R.id.ib_add_post);
        fabAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, AddPostActivity.class);
            startActivity(intent);
        });
    }

    private void fetchPostsFromFirestore()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    posts.clear();
                    for (QueryDocumentSnapshot document : querySnapshot)
                    {
                        Post post = document.toObject(Post.class);
                        post.setId(document.getId());
                        posts.add(post);
                    }
                    feedAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("FeedActivity", "Error fetching posts", e));
    }

    private void uploadPosts()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<Map<String, Object>> dummyPosts = new ArrayList<>();
        dummyPosts.add(createPostMap("Beach Cleanup", "Join us for a beach cleanup event!"));
        dummyPosts.add(createPostMap("Food Drive", "Help us distribute food to those in need."));
        dummyPosts.add(createPostMap("Tree Planting", "Be a part of our tree-planting initiative."));

        for (Map<String, Object> post : dummyPosts)
        {
            db.collection("posts")
                    .add(post)
                    .addOnSuccessListener(documentReference -> Log.d("UploadPosts", "Post added: " + documentReference.getId()))
                    .addOnFailureListener(e -> Log.w("UploadPosts", "Error adding post", e));
        }
    }
    private Map<String, Object> createPostMap(String title, String description) {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("title", title);
        postMap.put("description", description);
        postMap.put("comments", new ArrayList<String>());
        return postMap;
    }
}
