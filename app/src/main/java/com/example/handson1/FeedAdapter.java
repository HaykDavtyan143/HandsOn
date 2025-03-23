package com.example.handson1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private Context context;
    private List<Post> posts;

    private RecyclerView recyclerView;

    FirebaseAuth auth;
    FirebaseUser currentUser;

    public FeedAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.feed_item, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        String userId = currentUser.getUid();

        Post post = posts.get(position);
        holder.title.setText(post.getTitle());
        holder.description.setText(post.getDescription());
        holder.commentCount.setText(String.valueOf(post.getCommentsCount()));
        holder.likeCount.setText(String.valueOf(post.getLikes()));

        if (post.getId() == null || post.getId().isEmpty()) {
            Log.e("FeedAdapter", "Invalid Post ID: " + post.getTitle());
            return;
        }

        holder.commentButton.setOnClickListener(v -> {
            Log.d("FeedAdapter", "Context: " + context.getClass().getSimpleName());
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;

                try
                {
                    CommentsFragment commentsFragment = CommentsFragment.newInstance(post.getId());
                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.fragment_container, commentsFragment)
                            .addToBackStack(null)
                            .commit();
                }
                catch (Exception e)
                {
                    Log.e("FeedAdapter", "Error opening CommentsFragment: ", e);
                }
            }
            else
            {
                Log.e("FeedAdapter", "Context is not an AppCompatActivity!");
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("posts").document(post.getId());

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

    @Override
    public int getItemCount()
    {
        return posts.size();
    }

    public void updateCommentCountForPost(String postId, int newCommentCount)
    {
        for (int i = 0; i < posts.size(); i++)
        {
            Post post = posts.get(i);
            if (post.getId().equals(postId))
            {

                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
                if (viewHolder != null)
                {
                    FeedAdapter.FeedViewHolder holder = (FeedAdapter.FeedViewHolder) viewHolder;
                    holder.commentCount.setText(String.valueOf(newCommentCount));
                }
                break;
            }
        }
    }


    public int getCommentCount(String postId)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference postRef = db.collection("posts").document(postId);

        final int[] commentCount = {0};

        postRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                DocumentSnapshot document = task.getResult();
                if (document.exists())
                {
                    Map<String, Object> commentsMap = (Map<String, Object>) document.get("comments");
                    if (commentsMap != null)
                    {
                        commentCount[0] = commentsMap.size();
                    }
                }
            }
            else
            {
                Log.e("FeedAdapter", "Error getting post document: ", task.getException());
            }
        });

        return commentCount[0];
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder
    {
        TextView title, description, commentCount, likeCount;
        ImageButton commentButton, likeButton;

        public FeedViewHolder(@NonNull View itemView)
        {
            super(itemView);
            title = itemView.findViewById(R.id.post_title);
            description = itemView.findViewById(R.id.post_description);
            commentCount = itemView.findViewById(R.id.comment_count);
            likeCount = itemView.findViewById(R.id.like_count);
            commentButton = itemView.findViewById(R.id.comment_button);
            likeButton = itemView.findViewById(R.id.like_button);
        }
    }
}
