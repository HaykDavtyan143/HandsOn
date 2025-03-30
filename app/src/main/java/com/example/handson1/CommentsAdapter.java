package com.example.handson1;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>
{

    private HashMap<String, Object> comments;

    public CommentsAdapter(HashMap<String, Object> comments)
    {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position)
    {
        String commentKey = (String) comments.keySet().toArray()[position];
        String commentText = (String) comments.get(commentKey);

        holder.commentText.setText(commentText);
        holder.commentText.setTextColor(Color.BLACK);
    }

    @Override
    public int getItemCount()
    {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder
    {
        TextView commentText;
        public CommentViewHolder(@NonNull View itemView)
        {
            super(itemView);
            commentText = itemView.findViewById(R.id.comment_text);
            commentText.setTextColor(Color.BLACK);
        }
    }
}


