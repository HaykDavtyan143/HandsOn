package com.haykdavtyan.handson;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity
{

    private EditText etSearch;
    private ImageButton search;
    private Button btnUser, btnPost;
    private View lineV, lineH;
    private RecyclerView recyclerView;
    private List<Post> allPosts = new ArrayList<>();

    private SearchUserRecyclerAdapter userAdapter;
    private FeedAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearch = findViewById(R.id.etSearch);
        search = findViewById(R.id.ib_search);
        btnUser = findViewById(R.id.btnUser);
        btnPost = findViewById(R.id.btnPost);
        lineV = findViewById(R.id.lineV);
        lineH = findViewById(R.id.lineH);
        recyclerView = findViewById(R.id.searchRecyclerView);

        etSearch.requestFocus();

        // Initially hide user/post buttons and lines
        btnUser.setVisibility(View.GONE);
        btnPost.setVisibility(View.GONE);
        lineV.setVisibility(View.GONE);
        lineH.setVisibility(View.GONE);

        search.setOnClickListener(v -> {
            String searched = etSearch.getText().toString().trim();
            if (searched.isEmpty()) {
                etSearch.setError("This can't be empty");
                return;
            }
            // Show buttons and lines
            btnUser.setVisibility(View.VISIBLE);
            btnPost.setVisibility(View.VISIBLE);
            lineV.setVisibility(View.VISIBLE);
            lineH.setVisibility(View.VISIBLE);

            // Highlight Post button and reset User button color (default)
            btnPost.setTextColor(ContextCompat.getColor(this, R.color.handson));
            btnUser.setTextColor(ContextCompat.getColor(this, R.color.darkGray));

            setupPostSearchRecyclerView(searched);
        });

        btnUser.setOnClickListener(v -> {
            String searched = etSearch.getText().toString().trim();
            if (searched.isEmpty()) {
                etSearch.setError("This can't be empty");
                return;
            }
            btnUser.setTextColor(ContextCompat.getColor(this, R.color.handson));
            btnPost.setTextColor(ContextCompat.getColor(this, R.color.darkGray));
            setupUserSearchRecyclerView(searched);
        });

        btnPost.setOnClickListener(v -> {
            String searched = etSearch.getText().toString().trim();
            if (searched.isEmpty()) {
                etSearch.setError("This can't be empty");
                return;
            }
            btnPost.setTextColor(ContextCompat.getColor(this, R.color.handson));
            btnUser.setTextColor(ContextCompat.getColor(this, R.color.darkGray));
            setupPostSearchRecyclerView(searched);
        });
    }

    private void setupUserSearchRecyclerView(String searched) {
        // Stop and clear postAdapter if active
        if (postAdapter != null) {
            recyclerView.setAdapter(null);
            postAdapter = null;
        }

        Query query = FirebaseFirestore.getInstance()
                .collection("users")
                .orderBy("Username")
                .startAt(searched)
                .endAt(searched + "\uf8ff");

        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        userAdapter = new SearchUserRecyclerAdapter(options, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(userAdapter);
        userAdapter.startListening();
    }

    private void setupPostSearchRecyclerView(String searched) {
        // Stop and clear userAdapter if active
        if (userAdapter != null) {
            userAdapter.stopListening();
            recyclerView.setAdapter(null);
            userAdapter = null;
        }

        FirebaseFirestore.getInstance().collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                allPosts.clear();
                for (DocumentSnapshot doc : task.getResult()) {
                    Post post = doc.toObject(Post.class);
                    if (post != null) {
                        post.setId(doc.getId());
                        allPosts.add(post);
                    }
                }

                List<Post> filteredPosts = new ArrayList<>();
                String lowerSearched = searched.toLowerCase();

                for (Post post : allPosts) {
                    if (post.getTitle() != null && post.getTitle().toLowerCase().contains(lowerSearched)) {
                        filteredPosts.add(post);
                    }
                }

                if (postAdapter == null) {
                    postAdapter = new FeedAdapter(this, filteredPosts);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(postAdapter);
                } else {
                    postAdapter.updatePostList(filteredPosts);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (userAdapter != null) userAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userAdapter != null) userAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userAdapter != null) userAdapter.startListening();
    }
}
