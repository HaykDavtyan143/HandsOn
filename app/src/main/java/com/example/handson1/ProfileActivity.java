package com.example.handson1;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity
{

    private TextView Username, Type, bio;
    private Button btnFollowers, btnFollowing, btnPostsCount;
    private ImageButton btnPosts, btnLiked;
    private RecyclerView recyclerView;
    private FeedAdapter feedAdapter;
    protected List<Post> posts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Username = findViewById(R.id.username);
        Type = findViewById(R.id.userType);
        bio = findViewById(R.id.bio);
        btnPostsCount = findViewById(R.id.postsCount);
        btnFollowers = findViewById(R.id.followersCount);
        btnFollowing = findViewById(R.id.followingCount);
        btnPosts = findViewById(R.id.btnPosts);
        btnLiked = findViewById(R.id.btnLikedP);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(this, posts);
        recyclerView.setAdapter(feedAdapter);

        fetchMyPostsFromFirestore();

        bio.post(new Runnable()
        {
            @Override
            public void run()
            {
                int bioHeight = bio.getHeight();
                int marginTop = (int) (300 * getResources().getDisplayMetrics().density);
                marginTop += bioHeight;

                ViewGroup.MarginLayoutParams recyclerViewParams = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
                recyclerViewParams.topMargin = marginTop;
                recyclerView.setLayoutParams(recyclerViewParams);
            }
        });

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NavigationBarFragment())
                    .commit();
        }

        fetchCurrentUsername(username -> {
            Username.setText(username);
        });

        fetchCurrentAccType(accType -> {
            Type.setText(accType);
        });

        btnPosts.setOnClickListener(v -> {
            fetchMyPostsFromFirestore();
        });

        btnLiked.setOnClickListener(v -> {
            fetchLikedPostsFromFirestore();
        });

    }

    public void fetchMyPostsFromFirestore()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        fetchCurrentUsername(username -> {
            if (username == null)
            {
                Log.e("ProfileActivity", "Username is null. No posts will be shown.");
                return;
            }

            db.collection("posts")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        posts.clear();

                        for (QueryDocumentSnapshot document : querySnapshot)
                        {
                            try
                            {
                                Post post = document.toObject(Post.class);
                                post.setCreator(document.getString("creator"));

                                if (document.getId() != null)
                                {
                                    post.setId(document.getId());
                                }
                                else
                                {
                                    Log.e("ProfileActivity", "Post document ID is null: " + document.getData());
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
                                    Log.e("ProfileActivity", "Unexpected comments format: " + commentsObject);
                                }

                                Log.d("ProfileActivity", "Fetched post ID: " + post.getId() + ", Comments: " + post.getComments());

                                if (username.equals(document.getString("creator")))
                                    {
                                        posts.add(post);
                                    }

                                runOnUiThread(() -> btnPostsCount.setText(String.valueOf(posts.size())));

                            }
                            catch (Exception e)
                            {
                                Log.e("ProfileActivity", "Error processing post: " + document.getId(), e);
                            }
                        }

                        if (feedAdapter != null)
                        {
                            runOnUiThread(() -> {
                                feedAdapter.notifyDataSetChanged();
                                btnPostsCount.setText(String.valueOf(posts.size()));
                            });

                        }
                    })
                    .addOnFailureListener(e -> Log.e("ProfileActivity", "Error fetching posts", e));
        });
    }

    public void fetchLikedPostsFromFirestore()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        fetchCurrentUsername(username -> {
            if (username == null)
            {
                Log.e("ProfileActivity", "Username is null. No posts will be shown.");
                return;
            }

            db.collection("posts")
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        posts.clear();

                        for (QueryDocumentSnapshot document : querySnapshot)
                        {
                            try
                            {
                                Post post = document.toObject(Post.class);
                                post.setCreator(document.getString("creator"));

                                if (document.getId() != null)
                                {
                                    post.setId(document.getId());
                                }
                                else
                                {
                                    Log.e("ProfileActivity", "Post document ID is null: " + document.getData());
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
                                    Log.e("ProfileActivity", "Unexpected comments format: " + commentsObject);
                                }

                                Log.d("ProfileActivity", "Fetched post ID: " + post.getId() + ", Comments: " + post.getComments());

                                Map<String, Object> likedByMap = (Map<String, Object>) document.get("likedBy");
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                if (likedByMap.containsKey(user.getUid()))
                                {
                                    posts.add(post);
                                }
                            }
                            catch (Exception e)
                            {
                                Log.e("ProfileActivity", "Error processing post: " + document.getId(), e);
                            }
                        }

                        if (feedAdapter != null)
                        {
                            runOnUiThread(() -> {
                                feedAdapter.notifyDataSetChanged();
                            });

                        }
                    })
                    .addOnFailureListener(e -> Log.e("ProfileActivity", "Error fetching posts", e));
        });
    }

    private void fetchCurrentUsername(UsernameCallback callback)
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
