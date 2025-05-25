package com.haykdavtyan.handson;

import static android.app.PendingIntent.getActivity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder>
{
    private Context context;
    private List<Post> posts;

    private RecyclerView recyclerView;

    FirebaseAuth auth;
    FirebaseUser currentUser;

    public FeedAdapter(Context context, List<Post> posts)
    {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_item, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position)
    {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();

        Post post = posts.get(position);
        holder.creator.setText(post.getCreator());
        holder.title.setText(post.getTitle());
        holder.description.setText(post.getDescription());

        getCommentCount(post.getId(), new CommentCountCallback()
        {
            @Override
            public void onCommentCountFetched(int count)
            {
                post.setCommentsCount(count);
                holder.commentCount.setText(String.valueOf(count));
            }
        });


        holder.likeCount.setText(String.valueOf(post.getLikes()));

        holder.creatorType.setText(String.valueOf(post.getCreatorType()));

        holder.created.setText("Created: " + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(post.getCreationTime().toDate()));

        holder.expires.setText("Expires: " + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(post.getExpirationTime().toDate()));

        holder.category.setText("Category: " + post.getCategory());

        if (Timestamp.now().compareTo(post.getExpirationTime()) > 0)
        {
            holder.expired.setVisibility(View.VISIBLE);
        }

        if ((FirebaseAuth.getInstance().getCurrentUser().getUid().equals("fwRn6dA7tQMMCOmebDVzY8yUeKp2")) &&
                (Timestamp.now().compareTo(post.getExpirationTime()) <= 0))
        {
            holder.approve.setVisibility(View.VISIBLE);
            holder.delete.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.approve.setVisibility(View.GONE);
        }

        if (((FirebaseAuth.getInstance().getCurrentUser().getUid().equals(post.getCreatorId()))
                || (FirebaseAuth.getInstance().getCurrentUser().getUid().equals("fwRn6dA7tQMMCOmebDVzY8yUeKp2")))
                && (Timestamp.now().compareTo(post.getExpirationTime()) <= 0))
        {
            holder.delete.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.delete.setVisibility(View.GONE);
        }

        if (post.getId() == null || post.getId().isEmpty())
        {
            Log.e("FeedAdapter", "Invalid Post ID: " + post.getTitle());
            return;
        }

        holder.creator.setOnClickListener(v -> {
            Intent intent = new Intent(context, OtherUsersProfileActivity.class);
            intent.putExtra("creator", post.getCreator());
            intent.putExtra("creatorID", post.getCreatorId());
            intent.putExtra("creatorType", post.getCreatorType());
            context.startActivity(intent);
        });

        holder.commentButton.setOnClickListener(v -> {
            if (holder.commentSection.getVisibility() == View.VISIBLE) {
                holder.commentButton.setColorFilter(null);
                holder.commentSection.setVisibility(View.GONE);

                holder.commentInput.clearFocus();

                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && holder.commentInput != null) {
                    imm.hideSoftInputFromWindow(holder.commentInput.getWindowToken(), 0);
                }

            } else {
                holder.commentButton.setColorFilter(ContextCompat.getColor(context, R.color.handson), PorterDuff.Mode.SRC_IN);


                holder.commentSection.setVisibility(View.VISIBLE);

                holder.commentInput.requestFocus(); // focus on input
                holder.commentsRecyclerView.setNestedScrollingEnabled(true);

                holder.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                holder.commentsRecyclerView.setNestedScrollingEnabled(true);

                recyclerView.setNestedScrollingEnabled(true);

                // Let layout update first before scrolling
                holder.itemView.post(() -> {
                    if (recyclerView != null) {
                        recyclerView.smoothScrollToPosition(holder.getAdapterPosition());
                    }

                    // Show keyboard (optional)
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(holder.commentInput, InputMethodManager.SHOW_IMPLICIT);
                    }
                });

                // Setup Firestore references
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String postId = post.getId();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Setup comments list
                Map<String, Map<String, Object>> commentsMap = new LinkedHashMap<>();
                List<String> commentKeys = new ArrayList<>();

                CommentsAdapter commentsAdapter = new CommentsAdapter(context, commentsMap, commentKeys, postId, currentUserId);
                holder.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                holder.commentsRecyclerView.setAdapter(commentsAdapter);
                holder.commentsRecyclerView.setNestedScrollingEnabled(true);

                holder.commentsRecyclerView.setOnTouchListener((vv, event) -> {
                    vv.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                });

                // Load comments
                db.collection("posts").document(postId).get()
                        .addOnSuccessListener(doc -> {
                            Object raw = doc.get("comments");
                            if (raw instanceof Map) {
                                Map<String, Object> outer = (Map<String, Object>) raw;
                                commentsMap.clear();
                                commentKeys.clear();
                                for (Map.Entry<String, Object> e : outer.entrySet()) {
                                    if (e.getValue() instanceof Map) {
                                        commentsMap.put(e.getKey(), (Map<String, Object>) e.getValue());
                                    }
                                }
                                commentKeys.addAll(commentsMap.keySet());
                                commentsAdapter.notifyDataSetChanged();
                            }
                        });

                // Post new comment
                holder.postCommentButton.setOnClickListener(postView -> {
                    String text = holder.commentInput.getText().toString().trim();
                    if (text.isEmpty()) return;

                    String key = String.valueOf(System.currentTimeMillis());
                    Map<String,Object> commentData = new HashMap<>();
                    commentData.put("text", text);
                    commentData.put("creator", post.getCreator());  // you may need to pass this to the adapter
                    commentData.put("creatorId", currentUserId);
                    commentData.put("creatorType", String.valueOf(post.getCreatorType())); // same, pass this in or re-fetch
                    commentData.put("likes", 0L);
                    commentData.put("likedBy", new HashMap<String, Boolean>());

                    db.collection("posts").document(postId)
                            .update("comments." + key, commentData)
                            .addOnSuccessListener(a -> {
                                commentsMap.put(key, commentData);
                                commentKeys.clear();
                                commentKeys.addAll(commentsMap.keySet());
                                commentsAdapter.notifyDataSetChanged();
                                holder.commentsRecyclerView.scrollToPosition(commentKeys.size() - 1);
                                holder.commentInput.setText("");
                            });
                });
            }
        });



        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("posts").document(post.getId());

        holder.approve.setOnClickListener(v -> {
            postRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        holder.likeButton.setEnabled(false);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("approved", true);

                        postRef.update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    int positionToRemove = holder.getAdapterPosition();
                                    if (positionToRemove != RecyclerView.NO_POSITION) {
                                        posts.remove(positionToRemove);
                                        notifyItemRemoved(positionToRemove);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FeedAdapter", "Failed to update approval: ", e);
                                });
                    }
                }
            });
        });

        holder.delete.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setMessage("Are you sure you want to delete this post?")
                    .setPositiveButton("Delete", (dialogInterface, which) -> {
                        DocumentReference postRefDel = db.collection("posts").document(post.getId());

                        postRefDel.delete()
                                .addOnSuccessListener(aVoid -> {
                                    int positionToRemove = holder.getAdapterPosition();
                                    if (positionToRemove != RecyclerView.NO_POSITION) {
                                        posts.remove(positionToRemove);
                                        notifyItemRemoved(positionToRemove);
                                    }
                                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FeedAdapter", "Failed to delete post: ", e);
                                    Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancel", (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                    })
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                // Set Delete button color to red
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));

                // Set Cancel button color to black
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(context.getResources().getColor(android.R.color.black));

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
            });

            dialog.show();
        });

        postRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                DocumentSnapshot document = task.getResult();

                if (document.exists())
                {
                    Map<String, Object> likedByMap = (Map<String, Object>) document.get("likedBy");
                    if (likedByMap != null && likedByMap.containsKey(userId))
                    {
                        holder.likeButton.setImageResource(R.drawable.ic_liked);
                    }
                    else
                    {
                        holder.likeButton.setImageResource(R.drawable.ic_notliked);
                    }
                }
            }
        });

        holder.likeButton.setOnClickListener(v -> {
            postRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists())
                    {
                        Map<String, Object> likedByMap = (Map<String, Object>) document.get("likedBy");
                        if (likedByMap != null && likedByMap.containsKey(userId))
                        {
                            holder.likeButton.setEnabled(false);
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("likedBy." + userId, FieldValue.delete());

                            postRef.update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        postRef.update("likes", FieldValue.increment(-1));
                                        post.setLikes(post.getLikes() - 1);
                                        holder.likeCount.setText(String.valueOf(post.getLikes()));
                                        holder.likeButton.setImageResource(R.drawable.ic_notliked);
                                    });
                            holder.likeButton.setEnabled(true);
                        }
                        else
                        {
                            holder.likeButton.setEnabled(false);
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("likedBy." + userId, true);

                            postRef.update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        postRef.update("likes", FieldValue.increment(1));
                                        post.setLikes(post.getLikes() + 1);
                                        holder.likeCount.setText(String.valueOf(post.getLikes()));
                                        holder.likeButton.setImageResource(R.drawable.ic_liked);
                                    });
                            holder.likeButton.setEnabled(true);
                        }
                    }
                }
            });
        });
    }

    public void updatePostList(List<Post> newPosts)
    {
        this.posts = newPosts;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount()
    {
        return posts.size();
    }

    public void updatePosts(List<Post> newPosts)
    {
        this.posts.clear();
        this.posts.addAll(newPosts);
        notifyDataSetChanged();
    }


    public void updateCommentCountForPost(String postId, int newCommentCount)
    {
        for (int i = 0; i < posts.size(); i++)
        {
            Post post = posts.get(i);
            if (post.getId().equals(postId))
            {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                if (viewHolder != null) {
                    FeedViewHolder holder = (FeedViewHolder) viewHolder;
                    holder.commentCount.setText(String.valueOf(newCommentCount));
                }
                break;
            }
        }
    }

    public void getCommentCount(String postId, CommentCountCallback callback)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("posts").document(postId);

        postRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                DocumentSnapshot document = task.getResult();
                if (document.exists())
                {
                    Map<String, Object> commentsMap = (Map<String, Object>) document.get("comments");
                    int commentCount = commentsMap != null ? commentsMap.size() : 0;
                    callback.onCommentCountFetched(commentCount);
                }
                else
                {
                    Log.e("FeedAdapter", "Post not found!");
                }
            }
            else
            {
                Log.e("FeedAdapter", "Error getting post document: ", task.getException());
            }
        });
    }


    static class FeedViewHolder extends RecyclerView.ViewHolder
    {
        TextView creator, creatorType, created, expires, expired, title, description, commentCount, likeCount, category;
        ImageButton commentButton, likeButton, delete;
        Button approve;

        LinearLayout commentSection;
        RecyclerView commentsRecyclerView;
        EditText commentInput;
        ImageButton postCommentButton;

        public FeedViewHolder(@NonNull View itemView)
        {
            super(itemView);
            creator = itemView.findViewById(R.id.post_creator);
            title = itemView.findViewById(R.id.post_title);
            description = itemView.findViewById(R.id.post_description);
            commentCount = itemView.findViewById(R.id.comment_count);
            creatorType = itemView.findViewById(R.id.creatorType);
            created = itemView.findViewById(R.id.created);
            expires = itemView.findViewById(R.id.expires);
            expired = itemView.findViewById(R.id.expired);
            likeCount = itemView.findViewById(R.id.like_count);
            commentButton = itemView.findViewById(R.id.comment_button);
            category = itemView.findViewById(R.id.category);
            likeButton = itemView.findViewById(R.id.like_button);
            delete = itemView.findViewById(R.id.delete);
            approve = itemView.findViewById(R.id.approve);

            commentSection = itemView.findViewById(R.id.comment_section);
            commentsRecyclerView = itemView.findViewById(R.id.comments_recycler_view);
            commentInput = itemView.findViewById(R.id.comment_input);
            postCommentButton = itemView.findViewById(R.id.post_comment_button);
        }
    }

    public void setRecyclerView(RecyclerView recyclerView)
    {
        this.recyclerView = recyclerView;
    }

}
interface CommentCountCallback
{
    void onCommentCountFetched(int count);
}
