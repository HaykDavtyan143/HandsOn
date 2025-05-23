package com.haykdavtyan.handson;

import static android.app.PendingIntent.getActivity;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.HashMap;
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

        if (Timestamp.now().compareTo(post.getExpirationTime()) > 0)
        {
            holder.creator.setVisibility(View.GONE);
            holder.creatorType.setVisibility(View.GONE);
            holder.created.setVisibility(View.GONE);
            holder.expires.setVisibility(View.GONE);
            holder.title.setVisibility(View.GONE);
            holder.description.setVisibility(View.GONE);
            holder.likeButton.setVisibility(View.GONE);
            holder.likeCount.setVisibility(View.GONE);
            holder.commentButton.setVisibility(View.GONE);
            holder.commentCount.setVisibility(View.GONE);
            holder.approve.setVisibility(View.GONE);

            holder.expired.setVisibility(View.VISIBLE);

            holder.expired.setOnClickListener(v -> {
                holder.creator.setVisibility(View.VISIBLE);
                holder.creatorType.setVisibility(View.VISIBLE);
                holder.created.setVisibility(View.VISIBLE);
                holder.expires.setVisibility(View.VISIBLE);
                holder.title.setVisibility(View.VISIBLE);
                holder.description.setVisibility(View.VISIBLE);
                holder.likeButton.setVisibility(View.VISIBLE);
                holder.likeCount.setVisibility(View.VISIBLE);
                holder.commentButton.setVisibility(View.VISIBLE);
                holder.commentCount.setVisibility(View.VISIBLE);

                holder.expired.setVisibility(View.GONE);
            });
        }

        holder.likeCount.setText(String.valueOf(post.getLikes()));

        holder.creatorType.setText(String.valueOf(post.getCreatorType()));

        holder.created.setText("Created: " + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(post.getCreationTime().toDate()));

        holder.expires.setText("Expires: " + new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(post.getExpirationTime().toDate()));

        if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals("fwRn6dA7tQMMCOmebDVzY8yUeKp2"))
        {
            holder.approve.setVisibility(View.GONE);
        }
        else
        {
            holder.approve.setVisibility(View.VISIBLE);
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



        holder.commentButton.setOnClickListener(v -> {
            Log.d("FeedAdapter", "Context: " + context.getClass().getSimpleName());

            if (recyclerView != null && recyclerView.getLayoutManager() instanceof LinearLayoutManager)
            {
                int positionToScroll = holder.getAdapterPosition();
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(positionToScroll, 0);
            }
            if (context instanceof AppCompatActivity)
            {
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
        TextView creator, creatorType, created, expires, title, description, commentCount, likeCount, expired;
        ImageButton commentButton, likeButton;
        Button approve;
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
            likeButton = itemView.findViewById(R.id.like_button);
            approve = itemView.findViewById(R.id.approve);
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
