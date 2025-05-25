package com.haykdavtyan.handson;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FeedAdapter feedAdapter;
    protected List<Post> posts = new ArrayList<>();
    private ImageButton filterButton;

    private String lastSortBy = "newest";
    private String lastFilterType = "all";
    private  String lastFilterCategory = "all";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedAdapter = new FeedAdapter(this, posts);
        recyclerView.setAdapter(feedAdapter);
        feedAdapter.setRecyclerView(recyclerView);

        filterButton = findViewById(R.id.filter);
        filterButton.setOnClickListener(v -> openFilterDialog());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.getUid().equals("fwRn6dA7tQMMCOmebDVzY8yUeKp2")) {
            fetchModeratorPostFromFirestore();
        } else {
            fetchPostsFromFirestore();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NavigationBarFragment())
                    .commit();
        }
    }

    private void openFilterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        RadioGroup sortGroup = dialogView.findViewById(R.id.sort_group);
        RadioGroup typeGroup = dialogView.findViewById(R.id.type_group);
        RadioGroup categoryGroup = dialogView.findViewById(R.id.category_group);
        Button confirmButton = dialogView.findViewById(R.id.confirm_button);

        ScrollView scrollView = dialogView.findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.scrollTo(0, 0));


        // Restore previous selections
        switch (lastSortBy) {
            case "oldest":
                sortGroup.check(R.id.sort_oldest);
                break;
            case "most_liked":
                sortGroup.check(R.id.sort_best);
                break;
            case "newest":
            default:
                sortGroup.check(R.id.sort_newest);
                break;
        }

        switch (lastFilterType) {
            case "Volunteer":
                typeGroup.check(R.id.type_volunteer);
                break;
            case "Organization":
                typeGroup.check(R.id.type_organization);
                break;
            case "all":
            default:
                typeGroup.check(R.id.type_all);
                break;
        }

        switch (lastFilterCategory) {
            case "Environment":
                categoryGroup.check(R.id.category_environment);
                break;
            case "Education":
                categoryGroup.check(R.id.category_education);
                break;
            case "Health":
                categoryGroup.check(R.id.category_health);
                break;
            case "Animal Welfare":
                categoryGroup.check(R.id.category_animal_welfare);
                break;
            case "Community":
                categoryGroup.check(R.id.category_community);
                break;
            case "Arts and Culture":
                categoryGroup.check(R.id.category_arts);
                break;
            case "Sports":
                categoryGroup.check(R.id.category_sports);
                break;
            case "Technology":
                categoryGroup.check(R.id.category_technology);
                break;
            case "Human Rights":
                categoryGroup.check(R.id.category_human_rights);
                break;
            case "Disaster Relief":
                categoryGroup.check(R.id.category_disaster);
                break;
            case "Suggestion":
                categoryGroup.check(R.id.category_recommendation);
                break;
            case "all":
            default:
                categoryGroup.check(R.id.category_all);
                break;
        }


        confirmButton.setOnClickListener(v -> {
            int sortId = sortGroup.getCheckedRadioButtonId();
            int typeId = typeGroup.getCheckedRadioButtonId();
            int categoryId = categoryGroup.getCheckedRadioButtonId();

            String sortBy = "newest";
            String filterType = "all";
            String filterCategory = "all";

            if (sortId == R.id.sort_oldest) sortBy = "oldest";
            else if (sortId == R.id.sort_best) sortBy = "most_liked";

            if (typeId == R.id.type_volunteer) filterType = "Volunteer";
            else if (typeId == R.id.type_organization) filterType = "Organization";
            
            if (categoryId == R.id.category_environment) filterCategory = "Environment";
            else if (categoryId == R.id.category_education) filterCategory = "Education";
            else if (categoryId == R.id.category_health) filterCategory = "Health";
            else if (categoryId == R.id.category_animal_welfare) filterCategory = "Animal Welfare";
            else if (categoryId == R.id.category_community) filterCategory = "Community";
            else if (categoryId == R.id.category_arts) filterCategory = "Arts and Culture";
            else if (categoryId == R.id.category_sports) filterCategory = "Sports";
            else if (categoryId == R.id.category_technology) filterCategory = "Technology";
            else if (categoryId == R.id.category_human_rights) filterCategory = "Human Rights";
            else if (categoryId == R.id.category_disaster) filterCategory = "Disaster Relief";
            else if (categoryId == R.id.category_recommendation) filterCategory = "Suggestion";


            // Save last selections
            lastSortBy = sortBy;
            lastFilterType = filterType;
            lastFilterCategory = filterCategory;

            applyFilters(sortBy, filterType, filterCategory);
            dialog.dismiss();
        });

        dialog.show();
    }


    private void applyFilters(String sortBy, String filterType, String filterCategory) {
        List<Post> filteredPosts = new ArrayList<>();

        // Filter by type
        for (Post post : posts) {
            if (filterType.equals("all") || (post.getCreatorType() != null && post.getCreatorType().equals(filterType))) {
                if(filterCategory.equals("all") || (post.getCategory() != null && post.getCategory().equals(filterCategory))) {
                    filteredPosts.add(post);
                }
            }
        }

        // Sort
        switch (sortBy) {
            case "oldest":
                Collections.sort(filteredPosts, Comparator.comparing(Post::getCreationTime));
                break;
            case "most_liked":
                Collections.sort(filteredPosts, (p1, p2) -> Integer.compare(p2.getLikes(), p1.getLikes()));
                break;
            case "newest":
            default:
                Collections.sort(filteredPosts, (p1, p2) -> p2.getCreationTime().compareTo(p1.getCreationTime()));
                break;
        }

        feedAdapter = new FeedAdapter(this, filteredPosts);
        recyclerView.setAdapter(feedAdapter);
        feedAdapter.setRecyclerView(recyclerView);
        feedAdapter.notifyDataSetChanged();
    }

    public void fetchPostsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    posts.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        try {
                            Post post = document.toObject(Post.class);
                            post.setCreator(document.getString("creator"));
                            post.setCreatorId(document.getString("creatorID"));
                            post.setCreatorType(document.getString("creatorType"));
                            post.setApproved(document.getBoolean("approved"));
                            post.setCreationTime(document.getTimestamp("creationTime"));
                            post.setExpirationTime(document.getTimestamp("expirationTime"));
                            post.setCategory(document.getString("Category"));

                            if (document.getId() != null) {
                                post.setId(document.getId());
                            } else {
                                Log.e("FeedActivity", "Post document ID is null: " + document.getData());
                                continue;
                            }

                            Object commentsObject = document.get("comments");
                            if (commentsObject instanceof Map) {
                                HashMap<String, Object> fixedComments = new HashMap<>((Map<String, Object>) commentsObject);
                                post.setComments(fixedComments);
                            } else {
                                post.setComments(new HashMap<>());
                                Log.e("FeedActivity", "Unexpected comments format: " + commentsObject);
                            }

                            Log.d("FeedActivity", "Fetched post ID: " + post.getId() + ", Comments: " + post.getComments());

                            if ((document.getBoolean("approved")) && (Timestamp.now().compareTo(post.getExpirationTime()) <= 0)) {
                                posts.add(post);
                            }
                        } catch (Exception e) {
                            Log.e("FeedActivity", "Error processing post: " + document.getId(), e);
                        }
                    }

                    if (feedAdapter != null) {
                        runOnUiThread(() -> feedAdapter.notifyDataSetChanged());
                    }
                })
                .addOnFailureListener(e -> Log.e("FeedActivity", "Error fetching posts", e));
    }

    public void fetchModeratorPostFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .orderBy("creationTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    posts.clear();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        try {
                            if (!document.getBoolean("approved")) {
                                Post post = document.toObject(Post.class);
                                post.setCreator(document.getString("creator"));
                                post.setCreatorId(document.getString("creatorID"));
                                post.setCreatorType(document.getString("creatorType"));
                                post.setApproved(document.getBoolean("approved"));
                                post.setCreationTime(document.getTimestamp("creationTime"));
                                post.setExpirationTime(document.getTimestamp("expirationTime"));
                                post.setCategory(document.getString("Category"));

                                if (document.getId() != null) {
                                    post.setId(document.getId());
                                } else {
                                    Log.e("FeedActivity", "Post document ID is null: " + document.getData());
                                    continue;
                                }

                                Object commentsObject = document.get("comments");
                                if (commentsObject instanceof Map) {
                                    HashMap<String, Object> fixedComments = new HashMap<>((Map<String, Object>) commentsObject);
                                    post.setComments(fixedComments);
                                } else {
                                    post.setComments(new HashMap<>());
                                    Log.e("FeedActivity", "Unexpected comments format: " + commentsObject);
                                }

                                Log.d("FeedActivity", "Fetched post ID: " + post.getId() + ", Comments: " + post.getComments());
                                posts.add(post);
                            }
                        } catch (Exception e) {
                            Log.e("FeedActivity", "Error processing post: " + document.getId(), e);
                        }
                    }

                    if (feedAdapter != null) {
                        runOnUiThread(() -> feedAdapter.notifyDataSetChanged());
                    }
                })
                .addOnFailureListener(e -> Log.e("FeedActivity", "Error fetching posts", e));
    }
}
