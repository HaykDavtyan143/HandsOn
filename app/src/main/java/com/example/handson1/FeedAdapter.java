package com.example.handson1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder>
{
    private Context context;
    private List<Post> posts;

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
        Post post = posts.get(position);
        holder.title.setText(post.getTitle());
        holder.description.setText(post.getDescription());
        holder.commentCount.setText(post.getCommentsCount() + " comments");

        holder.commentButton.setOnClickListener(v -> {
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;

                if (post.getId() != null) {
                    CommentsFragment commentsFragment = CommentsFragment.newInstance(post.getId());

                    activity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, commentsFragment)
                            .addToBackStack(null)
                            .commit();
                } else {
                    Log.e("FeedAdapter", "Post ID is null for post: " + post.getTitle());
                }
            } else {
                Log.e("FeedAdapter", "Context is not an AppCompatActivity!");
            }
        });



    }

    @Override
    public int getItemCount()
    {
        return posts.size();
    }
    static class FeedViewHolder extends RecyclerView.ViewHolder
    {
        TextView title, description, commentCount;
        ImageButton commentButton;


        @SuppressLint("WrongViewCast")
        public FeedViewHolder(@NonNull View itemView)
        {
            super(itemView);
            title = itemView.findViewById(R.id.post_title);
            description = itemView.findViewById(R.id.post_description);
            commentCount = itemView.findViewById(R.id.comment_count);
            commentButton = itemView.findViewById(R.id.comment_button);
        }
    }
}

