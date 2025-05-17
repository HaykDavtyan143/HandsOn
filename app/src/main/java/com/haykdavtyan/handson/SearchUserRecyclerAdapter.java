package com.haykdavtyan.handson;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<User, SearchUserRecyclerAdapter.UserViewHolder> {
    Context context;
    FirebaseUser currentUser;
    FirebaseFirestore db;

    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<User> options, Context context) {
        super(options);
        this.context = context;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User user) {
        holder.username.setText(user.getUsername());
        holder.accType.setText(user.getAccType());

        if (currentUser == null) {
            holder.follow.setVisibility(View.GONE);
            return;
        }

        // Initially set button UI based on follow status:
        checkFollowStatus(user.getId(), holder.follow, user.getUsername());

        // Handle follow button clicks:
        holder.follow.setOnClickListener(v -> {
            handleFollowButtonClick(user.getId(), user.getUsername(), holder.follow);
        });

        holder.username.setOnClickListener(v -> {
            Intent intent = new Intent (context, OtherUsersProfileActivity.class);
            intent.putExtra("creatorID", user.getId());
            intent.putExtra("creatorType", user.getAccType());
            intent.putExtra("creator", user.getUsername());
            context.startActivity(intent);
            ((android.app.Activity) context).finish();
        });
    }

    private void checkFollowStatus(String otherUserId, Button btnFollow, String otherUsername)
    {
        if (currentUser == null || otherUserId == null) {
            btnFollow.setVisibility(View.GONE);
            return;
        }

        if (currentUser.getUid().trim().equals(otherUserId.trim()))
        {
            btnFollow.setText("(Me)");
            btnFollow.setBackgroundTintList(null);
            btnFollow.setTextColor(ContextCompat.getColor(context, R.color.darkGray));
            btnFollow.setBackground(null);
        }
        else
        {
            DocumentReference currentUserRef = db.collection("users").document(currentUser.getUid());
            currentUserRef.get().addOnSuccessListener(snapshot -> {
                if (!snapshot.exists()) return;

                User currentUserObj = snapshot.toObject(User.class);
                if (currentUserObj == null) return;

                HashMap<String, String> followingMap = currentUserObj.getFollowing();
                if (followingMap != null && followingMap.containsKey(otherUserId))
                {
                    btnFollow.setText("Following");
                    btnFollow.setBackgroundTintList(null);
                    btnFollow.setTextColor(ContextCompat.getColor(context, R.color.background_light));
                    btnFollow.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_button_following));
                }
                else
                {
                    btnFollow.setText("Follow");
                    btnFollow.setBackgroundTintList(null);
                    btnFollow.setTextColor(ContextCompat.getColor(context, R.color.background_light));
                    btnFollow.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_button));
                }
            }).addOnFailureListener(e -> Log.e("FollowStatus", "Failed to check follow status", e));
        }
    }

    private void handleFollowButtonClick(String otherUserId, String otherUsername, Button btnFollow)
    {
        if (currentUser == null || otherUserId == null) return;

        if (currentUser.getUid().trim().equals(otherUserId.trim()))
        {
            return;
        }
        else
        {
            String currentUserId = currentUser.getUid();
            DocumentReference currentUserRef = db.collection("users").document(currentUserId);
            DocumentReference otherUserRef = db.collection("users").document(otherUserId);

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

                    boolean isFollowing = currentFollowing != null && currentFollowing.containsKey(otherUserId);

                    if (!isFollowing) {
                        // Add follow
                        if (currentFollowing == null) currentFollowing = new HashMap<>();
                        if (otherFollowers == null) otherFollowers = new HashMap<>();

                        currentFollowing.put(otherUserId, otherUsername);
                        otherFollowers.put(currentUserId, currentUserObj.getUsername());

                        currentUserObj.setFollowing(currentFollowing);
                        otherUserObj.setFollowers(otherFollowers);

                        btnFollow.setText("Following");
                        btnFollow.setTextColor(ContextCompat.getColor(context, R.color.background_light));
                        btnFollow.setBackgroundTintList(null);
                        btnFollow.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_button_following));
                    }
                    else
                    {
                        // Unfollow
                        if (currentFollowing != null) currentFollowing.remove(otherUserId);
                        if (otherFollowers != null) otherFollowers.remove(currentUserId);

                        currentUserObj.setFollowing(currentFollowing);
                        otherUserObj.setFollowers(otherFollowers);

                        btnFollow.setText("Follow");
                        btnFollow.setBackgroundTintList(null);
                        btnFollow.setTextColor(ContextCompat.getColor(context, R.color.background_light));
                        btnFollow.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_button));
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
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserViewHolder(view);
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        Button username, follow;
        TextView accType;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.user);
            accType = itemView.findViewById(R.id.userType);
            follow = itemView.findViewById(R.id.follow);
        }
    }
}
