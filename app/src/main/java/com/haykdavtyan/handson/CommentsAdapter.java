package com.haykdavtyan.handson;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private Map<String, Map<String, Object>> commentsMap;
    private List<String> commentKeys;
    private String postId;
    private String currentUserId;
    private FirebaseFirestore db;
    private Context context;

    public CommentsAdapter(
            Context context,
            Map<String, Map<String, Object>> commentsMap,
            List<String> commentKeys,
            String postId,
            String currentUserId
    ) {
        this.context = context;
        this.commentsMap = commentsMap;
        this.commentKeys = commentKeys;
        this.postId = postId;
        this.currentUserId = currentUserId;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        String commentKey = commentKeys.get(position);
        Map<String, Object> data = commentsMap.get(commentKey);
        DocumentReference postRef = db.collection("posts").document(postId);
        String path = "comments." + commentKey;

        // 1) Bind text & creator
        if (data != null) {
            String text = data.get("text") != null ? (String) data.get("text") : "";
            holder.commentText.setText(text);
            String creator = data.get("creator") != null ? (String) data.get("creator") : null;
            if (creator != null) {
                holder.creatorNameButton.setVisibility(View.VISIBLE);
                holder.creatorNameButton.setText(creator);
            } else {
                holder.creatorNameButton.setVisibility(View.GONE);
            }

            String creatorId = data.get("creatorId") != null ? (String) data.get("creatorId") : null;
            String creatorType = data.get("creatorType") != null ? (String) data.get("creatorType") : null;

            holder.creatorNameButton.setOnClickListener(v ->{
                Intent intent = new Intent(context, OtherUsersProfileActivity.class);
                intent.putExtra("creatorID", creatorId);
                intent.putExtra("creator", creator);
                intent.putExtra("creatorType", creatorType);
                context.startActivity(intent);
            });

            if ((FirebaseAuth.getInstance().getCurrentUser().getUid().equals(creatorId))
                    || (FirebaseAuth.getInstance().getCurrentUser().getUid().equals("fwRn6dA7tQMMCOmebDVzY8yUeKp2")))
            {
                holder.delete.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.delete.setVisibility(View.GONE);
            }

            holder.delete.setOnClickListener(v -> {
                androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setMessage("Are you sure you want to delete this comment?")
                        .setPositiveButton("Delete", (dialogInterface, which) -> {
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("comments." + commentKey, FieldValue.delete());

                            db.collection("posts").document(postId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();

                                        commentsMap.remove(commentKey);
                                        commentKeys.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, commentKeys.size());
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Cancel", (dialogInterface, which) -> {
                            // This dismisses the dialog immediately on cancel click
                            dialogInterface.dismiss();
                        })
                        .create();

                dialog.setOnShowListener(dialogInterface -> {
                    // Set Delete button red color
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));

                    // Set Cancel button black color
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(context.getResources().getColor(android.R.color.black));
                });

                dialog.show();
            });
        }

        // 2) Load likes & likedBy
        postRef.get().addOnSuccessListener(doc -> {
            Long rawLikes = doc.getLong(path + ".likes");
            long likes = rawLikes == null ? 0L : rawLikes;
            holder.likeCount.setText(String.valueOf(likes));

            @SuppressWarnings("unchecked")
            Map<String, Boolean> likedBy = (Map<String, Boolean>) doc.get(path + ".likedBy");
            boolean isLiked = likedBy != null && Boolean.TRUE.equals(likedBy.get(currentUserId));
            holder.likeButton.setImageResource(isLiked ? R.drawable.ic_liked : R.drawable.ic_notliked);
        });

        // 3) Toggle like
        holder.likeButton.setOnClickListener(v -> {
            postRef.get().addOnSuccessListener(doc -> {
                Long raw2 = doc.getLong(path + ".likes");
                long count2 = raw2 == null ? 0L : raw2;
                @SuppressWarnings("unchecked")
                Map<String, Boolean> likedBy2 = (Map<String, Boolean>) doc.get(path + ".likedBy");
                boolean isLiked2 = likedBy2 != null && Boolean.TRUE.equals(likedBy2.get(currentUserId));

                Map<String, Object> updates = new HashMap<>();
                if (isLiked2) {
                    updates.put(path + ".likedBy." + currentUserId, FieldValue.delete());
                    updates.put(path + ".likes", FieldValue.increment(-1));
                } else {
                    updates.put(path + ".likedBy." + currentUserId, true);
                    updates.put(path + ".likes", FieldValue.increment(1));
                }

                postRef.update(updates)
                        .addOnSuccessListener(aVoid -> {
                            long newCount = isLiked2 ? count2 - 1 : count2 + 1;
                            holder.likeCount.setText(String.valueOf(newCount));
                            holder.likeButton.setImageResource(isLiked2 ? R.drawable.ic_notliked : R.drawable.ic_liked);
                        })
                        .addOnFailureListener(e -> Log.e("CommentsAdapter", "toggle like failed", e));
            });
        });
    }

    @Override
    public int getItemCount() {
        return commentKeys.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentText;
        Button creatorNameButton;
        ImageButton likeButton, delete;
        TextView likeCount;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentText = itemView.findViewById(R.id.comment_text);
            creatorNameButton = itemView.findViewById(R.id.creator_name_button);
            likeButton = itemView.findViewById(R.id.like_button);
            delete = itemView.findViewById(R.id.delete);
            likeCount = itemView.findViewById(R.id.like_count);
        }
    }
}
