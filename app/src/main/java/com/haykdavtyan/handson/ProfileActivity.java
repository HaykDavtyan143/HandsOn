package com.haykdavtyan.handson;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity
{

    private TextView Username, Type, bio;
    private Button btnFollowers, btnFollowing, btnPostsCount;
    private ImageButton btnPosts, btnLiked, settings;
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
        setContentView(R.layout.activity_profile);

        Username = findViewById(R.id.username);
        Type = findViewById(R.id.userType);
        bio = findViewById(R.id.bio);
        btnPostsCount = findViewById(R.id.postsCount);
        btnFollowers = findViewById(R.id.followersCount);
        btnFollowing = findViewById(R.id.followingCount);
        btnPosts = findViewById(R.id.btnPosts);
        btnLiked = findViewById(R.id.btnLikedP);
        settings = findViewById(R.id.settings_button);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(this, posts);
        recyclerView.setAdapter(feedAdapter);

        recyclerView.bringToFront();
        recyclerView.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        fetchMyPostsFromFirestore();
        loadFollowerAndFollowingCounts();

       bio.post(new Runnable()
        {
            @Override
            public void run()
            {
                int bioHeight = bio.getHeight();
                int marginTop = (int) (250 * getResources().getDisplayMetrics().density);
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

        fetchCurrentBio(fetchedBio -> {
            bio.setText(fetchedBio);
        });

        btnPosts.setOnClickListener(v -> {
            fetchMyPostsFromFirestore();
        });

        btnLiked.setOnClickListener(v -> {
            fetchLikedPostsFromFirestore();
        });

        btnFollowers.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FollowersActivity.class);
            intent.putExtra("userId", currentUser.getUid());
            startActivity(intent);
            finish();
        });

        btnFollowing.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FollowingActivity.class);
            intent.putExtra("userId", currentUser.getUid());
            startActivity(intent);
            finish();
        });

        settings.setOnClickListener(view -> {
            View popupView = LayoutInflater.from(this).inflate(R.layout.menu_popup, null);
            PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setElevation(10);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.showAsDropDown(settings, -30, 10); // Adjust offset as needed

            popupView.findViewById(R.id.edit_profile).setOnClickListener(v -> {
                startActivity(new Intent(this, EditProfileActivity.class));
                popupWindow.dismiss();
            });

            popupView.findViewById(R.id.logout).setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                popupWindow.dismiss();
            });

            popupView.findViewById(R.id.delete_account).setOnClickListener(v -> {
                AlertDialog dialog = new AlertDialog.Builder(ProfileActivity.this)
                        .setMessage("Are you sure you want to delete your profile?")
                        .setPositiveButton("Delete", (dialogInterface, which) -> {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user == null) return;

                            String uid = user.getUid();
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            db.collection("users").document(uid).delete()
                                    .addOnSuccessListener(aVoid -> db.collection("posts")
                                            .whereEqualTo("creatorId", uid)
                                            .get()
                                            .addOnSuccessListener(postSnapshots -> {
                                                WriteBatch postBatch = db.batch();
                                                for (DocumentSnapshot doc : postSnapshots) {
                                                    postBatch.delete(doc.getReference());
                                                }
                                                postBatch.commit()
                                                        .addOnSuccessListener(unused -> db.collection("chatrooms")
                                                                .whereArrayContains("userIds", uid)
                                                                .get()
                                                                .addOnSuccessListener(chatSnapshots -> {
                                                                    WriteBatch chatBatch = db.batch();
                                                                    for (DocumentSnapshot doc : chatSnapshots) {
                                                                        chatBatch.delete(doc.getReference());
                                                                    }
                                                                    chatBatch.commit()
                                                                            .addOnSuccessListener(unused2 -> db.collection("posts")
                                                                                    .get()
                                                                                    .addOnSuccessListener(allPosts -> {
                                                                                        WriteBatch commentBatch = db.batch();
                                                                                        for (DocumentSnapshot post : allPosts) {
                                                                                            Map<String, Object> comments = (Map<String, Object>) post.get("comments");
                                                                                            if (comments != null) {
                                                                                                Map<String, Object> updatedComments = new HashMap<>(comments);
                                                                                                boolean changed = false;
                                                                                                for (Map.Entry<String, Object> entry : comments.entrySet()) {
                                                                                                    Map<String, Object> commentData = (Map<String, Object>) entry.getValue();
                                                                                                    if (uid.equals(commentData.get("creatorId"))) {
                                                                                                        updatedComments.remove(entry.getKey());
                                                                                                        changed = true;
                                                                                                    }
                                                                                                }
                                                                                                if (changed) {
                                                                                                    commentBatch.update(post.getReference(), "comments", updatedComments);
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        commentBatch.commit()
                                                                                                .addOnSuccessListener(unused3 -> user.delete()
                                                                                                        .addOnSuccessListener(unused4 -> {
                                                                                                            Toast.makeText(ProfileActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                                                                                                            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                                                                                                            finish();
                                                                                                        })
                                                                                                        .addOnFailureListener(e ->
                                                                                                                Toast.makeText(ProfileActivity.this, "Auth delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                                                                        ))
                                                                                                .addOnFailureListener(e ->
                                                                                                        Toast.makeText(ProfileActivity.this, "Failed deleting comments: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                                                                );
                                                                                    })
                                                                                    .addOnFailureListener(e ->
                                                                                            Toast.makeText(ProfileActivity.this, "Error getting posts for comment cleanup: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                                                    ))
                                                                            .addOnFailureListener(e ->
                                                                                    Toast.makeText(ProfileActivity.this, "Failed deleting chatrooms: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                                            );
                                                                })
                                                                .addOnFailureListener(e ->
                                                                        Toast.makeText(ProfileActivity.this, "Failed getting chatrooms: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                                ))
                                                        .addOnFailureListener(e ->
                                                                Toast.makeText(ProfileActivity.this, "Failed deleting posts: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                        );
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(ProfileActivity.this, "Error getting user posts: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                            ))
                                    .addOnFailureListener(e ->
                                            Toast.makeText(ProfileActivity.this, "Failed deleting user doc: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );

                            dialogInterface.dismiss();
                            popupWindow.dismiss();
                        })
                        .setNegativeButton("Cancel", (dialogInterface, which) -> {
                            dialogInterface.dismiss();
                        })
                        .create();

                dialog.setOnShowListener(dialogInterface -> {
                    AlertDialog alertDialog = (AlertDialog) dialogInterface;
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(getResources().getColor(android.R.color.black));
                });

                dialog.show();
            });


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

                                if ((username.equals(document.getString("creator"))) && (document.getBoolean("approved")))
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

    private void fetchCurrentBio(BioCallback callback)
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

    private void loadFollowerAndFollowingCounts()
    {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser.getUid() == null) return;

        db.collection("users").document(currentUser.getUid())
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
