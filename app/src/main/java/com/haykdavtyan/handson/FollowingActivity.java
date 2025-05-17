package com.haykdavtyan.handson;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;

public class FollowingActivity extends AppCompatActivity
{

    RecyclerView recyclerView;
    SearchUserRecyclerAdapter adapter;
    FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Log.e("FollowingActivity", "No userId provided");
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user == null || user.getFollowing() == null) return;

                    HashMap<String, String> followingMap = user.getFollowing();
                    if (followingMap.isEmpty()) return;

                    Query query = db.collection("users").whereIn("id", new java.util.ArrayList<>(followingMap.keySet()));

                    FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                            .setQuery(query, User.class)
                            .build();

                    adapter = new SearchUserRecyclerAdapter(options, this);
                    recyclerView.setAdapter(adapter);
                    adapter.startListening();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}
