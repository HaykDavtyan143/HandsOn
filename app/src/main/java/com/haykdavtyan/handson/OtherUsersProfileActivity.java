package com.haykdavtyan.handson;

import static androidx.core.view.ViewCompat.setBackground;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherUsersProfileActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private String creator, creatorID, creatorType;
    private TextView Username, Type, bio;
    private Button btnFollowers, btnFollowing, btnPostsCount, btnFollow, btnMessage;
    private RecyclerView recyclerView;
    private FeedAdapter feedAdapter;
    protected List<Post> posts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_users_profile);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Username = findViewById(R.id.username);
        Type = findViewById(R.id.userType);
        bio = findViewById(R.id.bio);
        btnPostsCount = findViewById(R.id.postsCount);
        btnFollowers = findViewById(R.id.followersCount);
        btnFollowing = findViewById(R.id.followingCount);
        btnFollow = findViewById(R.id.follow);
        btnMessage = findViewById(R.id.message);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(this, posts);
        recyclerView.setAdapter(feedAdapter);

        creator = getIntent().getStringExtra("creator");
        creatorID = getIntent().getStringExtra("creatorID");
        creatorType = getIntent().getStringExtra("creatorType");

        fetchUsersPostsFromFirestore(creator);
        checkFollowStatus();
        loadFollowerAndFollowingCounts();

        Username.setText(creator);
        Type.setText(creatorType);

        if (creatorID != null && currentUser != null && currentUser.getUid() != null)
        {
            if (currentUser.getUid().trim().equals(creatorID.trim()))
            {
                Intent intent = new Intent(OtherUsersProfileActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                Log.d("DEBUG", "UIDs do not match");
            }
        }
        else
        {
            Log.e("DEBUG", "creatorID or currentUser is null!");
        }

        bio.post(new Runnable()
        {
            @Override
            public void run()
            {
                int bioHeight = bio.getHeight();
                int marginTop = (int) (270 * getResources().getDisplayMetrics().density);
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

        fetchCurrentBio(fetchedBio -> {
            if (fetchedBio != null)
            {
                bio.setText(fetchedBio);
            }
            else
            {
                bio.setText("None");
            }
        }, creatorID);

        btnFollowers.setOnClickListener(v -> {
            Intent intent = new Intent(OtherUsersProfileActivity.this, FollowersActivity.class);
            intent.putExtra("userId", creatorID);
            startActivity(intent);
            finish();
        });

        btnFollowing.setOnClickListener(v -> {
            Intent intent = new Intent(OtherUsersProfileActivity.this, FollowingActivity.class);
            intent.putExtra("userId", creatorID);
            startActivity(intent);
            finish();
        });

        btnMessage.setOnClickListener(v -> {
            Intent intent = new Intent(OtherUsersProfileActivity.this, ChatActivity.class);
            intent.putExtra("userID", creatorID);
            intent.putExtra("username", creator);
            intent.putExtra("type",creatorType);
            startActivity(intent);
            finish();
        });

        btnFollow.setOnClickListener(v -> handleFollowButtonClick());
    }

    private void fetchCurrentBio(BioCallback callback, String userId)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists())
                {
                    DocumentSnapshot document = task.getResult();
                    String fetchedBio = document.getString("Bio");
                    callback.onBioRetrieved(fetchedBio);
                }
                else
                {
                    callback.onBioRetrieved(null);
                }
            });
        }
        else
        {
            callback.onBioRetrieved(null);
        }
    }
    public void fetchUsersPostsFromFirestore(String username)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("posts")
                    .orderBy("creationTime", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        posts.clear();

                        for (QueryDocumentSnapshot document : querySnapshot)
                        {
                            try
                            {
                                Post post = document.toObject(Post.class);
                                post.setCreator(document.getString("creator"));
                                post.setCreatorId(document.getString("creatorID"));
                                post.setCreatorType(document.getString("creatorType"));
                                post.setApproved((document.getBoolean("approved")));
                                post.setCategory(document.getString("Category"));
                                post.setCreationTime(document.getTimestamp("creationTime"));
                                post.setExpirationTime(document.getTimestamp("expirationTime"));

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

                                if ((username.equals(document.getString("creator"))) && document.getBoolean("approved"))
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
    }

    private void checkFollowStatus()
    {
        if (currentUser == null || creatorID == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    User currentUserObj = snapshot.toObject(User.class);
                    if (currentUserObj == null) return;

                    HashMap<String, String> followingMap = currentUserObj.getFollowing();

                    if (followingMap.containsKey(creatorID))
                    {
                        btnFollow.setText("Following");
                        btnFollow.setBackgroundTintList(null);
                        btnFollow.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_following));
                    }
                    else
                    {
                        btnFollow.setText("Follow");
                        btnFollow.setBackgroundTintList(null);
                        btnFollow.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button));
                    }
                })
                .addOnFailureListener(e -> Log.e("FollowStatus", "Failed to check follow status", e));
    }

    private void handleFollowButtonClick()
    {
        if (currentUser == null || creatorID == null) return;

        String currentUserId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference currentUserRef = db.collection("users").document(currentUserId);
        DocumentReference otherUserRef = db.collection("users").document(creatorID);

        currentUserRef.get().addOnSuccessListener(currentSnapshot -> {
            if (!currentSnapshot.exists()) return;

            User currentUserObj = currentSnapshot.toObject(User.class);
            if (currentUserObj == null) return;

            otherUserRef.get().addOnSuccessListener(otherSnapshot -> {
                if (!otherSnapshot.exists()) return;

                User otherUserObj = otherSnapshot.toObject(User.class);
                if (otherUserObj == null) return;

                HashMap<String, String> currentFollowing = currentUserObj.getFollowing();
                HashMap<String, String> otherFollowers = otherUserObj.getFollowers();

                boolean isFollowing = currentFollowing.containsKey(creatorID);

                if (!isFollowing)
                {
                    // Add follow
                    currentFollowing.put(creatorID, creator);
                    otherFollowers.put(currentUserId, currentUserObj.getUsername());

                    currentUserObj.setFollowing(currentFollowing);
                    otherUserObj.setFollowers(otherFollowers);

                    btnFollow.setText("Following");
                    btnFollow.setBackgroundTintList(null);
                    btnFollow.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_following));
                }

                else
                {
                    // Unfollow
                    currentFollowing.remove(creatorID);
                    otherFollowers.remove(currentUserId);

                    currentUserObj.setFollowing(currentFollowing);
                    otherUserObj.setFollowers(otherFollowers);

                    btnFollow.setText("Follow");
                    btnFollow.setBackgroundTintList(null);
                    btnFollow.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button));
                }

                // Save both users back to Firestore
                currentUserRef.set(currentUserObj)
                        .addOnSuccessListener(aVoid -> Log.d("Follow", "Updated current user following"))
                        .addOnFailureListener(e -> Log.e("Follow", "Failed to update current user", e));

                otherUserRef.set(otherUserObj)
                        .addOnSuccessListener(aVoid -> Log.d("Follow", "Updated other user followers"))
                        .addOnFailureListener(e -> Log.e("Follow", "Failed to update other user", e));
            });
        });
    }

    private void loadFollowerAndFollowingCounts()
    {
        if (creatorID == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(creatorID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    User user = documentSnapshot.toObject(User.class);
                    if (user == null) return;

                    int followersCount = user.getFollowers() != null ? user.getFollowers().size() : 0;
                    int followingCount = user.getFollowing() != null ? user.getFollowing().size() : 0;

                    btnFollowers.setText(String.valueOf(followersCount));
                    btnFollowing.setText(String.valueOf(followingCount));
                })
                .addOnFailureListener(e -> Log.e("UserCounts", "Failed to load follower/following counts", e));
    }

}